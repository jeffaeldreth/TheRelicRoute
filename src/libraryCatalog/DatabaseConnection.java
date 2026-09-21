package libraryCatalog;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class DatabaseConnection {

    private static ConnectionSettings settings;
    private static Session sshSession;
    private static Connection dbConnection;

    private static Consumer<String> errorReporter;
    private static long lastReportMillis = 0;

    // A live connection plus the SSH tunnel it runs through (if any).
    private static class Opened {
        Session ssh;
        Connection connection;

        void close() {
            try {
                if (connection != null) connection.close();
            } catch (Exception ignored) {
            }
            if (ssh != null) {
                ssh.disconnect();
            }
        }
    }

    // Asks the SSH server's identity question ("do you trust this host?") through a dialog.
    private static class HostKeyPrompt implements UserInfo {
        @Override public String getPassphrase() { return null; }
        @Override public String getPassword() { return null; }
        @Override public boolean promptPassword(String message) { return false; }
        @Override public boolean promptPassphrase(String message) { return false; }
        @Override public void showMessage(String message) { }

        @Override
        public boolean promptYesNo(String message) {
            if (Platform.isFxApplicationThread()) {
                return askYesNo(message);
            }
            FutureTask<Boolean> task = new FutureTask<>(() -> askYesNo(message));
            Platform.runLater(task);
            try {
                return task.get();
            } catch (Exception e) {
                return false;
            }
        }

        private boolean askYesNo(String message) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            alert.setTitle("Trust this SSH server?");
            alert.setHeaderText("First time connecting to this SSH server");
            alert.getDialogPane().getStylesheets().add(DatabaseConnection.class.getResource("Style.css").toExternalForm());
            Optional<ButtonType> answer = alert.showAndWait();
            return answer.isPresent() && answer.get() == ButtonType.YES;
        }
    }

    // Uses these settings from now on. Any connection made with the old settings is closed.
    public static synchronized void configure(ConnectionSettings newSettings) {
        disconnect();
        settings = newSettings == null ? null : newSettings.copy();
    }

    public static synchronized ConnectionSettings getSettings() {
        return settings == null ? null : settings.copy();
    }

    // Called (on the JavaFX thread) when a connection attempt fails, so the user sees why.
    public static void setErrorReporter(Consumer<String> reporter) {
        errorReporter = reporter;
    }

    // synchronized: two parts of the app must never open the tunnel at the same time.
    public static synchronized Connection getConnection() throws Exception {
        if (dbConnection != null && !dbConnection.isClosed()) {
            return dbConnection;
        }
        if (settings == null) {
            IllegalStateException e = new IllegalStateException(
                    "The database connection isn't set up yet. Open File > Database Connection... to set it up.");
            report(e);
            throw e;
        }
        try {
            Opened opened = open(settings);
            sshSession = opened.ssh;
            dbConnection = opened.connection;
        } catch (Exception e) {
            report(e);
            throw e;
        }
        return dbConnection;
    }

    // Tries the given settings without changing the app's real connection.
    // Returns null if it worked, or a message describing what went wrong.
    public static String test(ConnectionSettings toTest) {
        Opened opened = null;
        try {
            opened = open(toTest);
            return null;
        } catch (Exception e) {
            return describe(e);
        } finally {
            if (opened != null) {
                opened.close();
            }
        }
    }

    private static Opened open(ConnectionSettings s) throws Exception {
        String problem = s.validate();
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }

        Opened opened = new Opened();
        try {
            String host = s.dbHost.trim();
            int port = s.dbPort;

            if (s.sshEnabled) {
                JSch jsch = new JSch();
                jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
                if (!s.sshKeyFile.trim().isEmpty()) {
                    jsch.addIdentity(s.sshKeyFile.trim(), s.sshPassphrase.isEmpty() ? null : s.sshPassphrase);
                }
                opened.ssh = jsch.getSession(s.sshUser.trim(), s.sshHost.trim(), s.sshPort);
                if (!s.sshPassword.isEmpty()) {
                    opened.ssh.setPassword(s.sshPassword);
                }
                opened.ssh.setUserInfo(new HostKeyPrompt());
                opened.ssh.connect(15000);
                // Local port 0 = let the system pick a free one, so nothing ever clashes with another program.
                port = opened.ssh.setPortForwardingL(0, host, port);
                host = "127.0.0.1";
            }

            Properties credentials = new Properties();
            credentials.setProperty("user", s.dbUser.trim());
            credentials.setProperty("password", s.dbPassword);
            String url = "jdbc:mariadb://" + host + ":" + port + "/" + s.dbName.trim() + "?connectTimeout=10000";
            opened.connection = DriverManager.getConnection(url, credentials);
            return opened;
        } catch (Exception e) {
            opened.close();
            throw e;
        }
    }

    public static synchronized void disconnect() {
        try {
            if (dbConnection != null) dbConnection.close();
        } catch (Exception ignored) {
        }
        dbConnection = null;
        if (sshSession != null) {
            sshSession.disconnect();
            sshSession = null;
        }
    }

    // Shows a failed attempt to the user, but not more than once every few seconds.
    private static void report(Exception e) {
        Consumer<String> reporter = errorReporter;
        long now = System.currentTimeMillis();
        if (reporter == null || now - lastReportMillis < 8000) {
            return;
        }
        lastReportMillis = now;
        String message = describe(e);
        Platform.runLater(() -> reporter.accept(message));
    }

    // The most specific message in the chain of causes (for example "Access denied for user ...").
    public static String describe(Throwable e) {
        String best = null;
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null && !t.getMessage().isBlank()) {
                best = t.getMessage();
            }
        }
        return best != null ? best : e.getClass().getSimpleName();
    }
}
