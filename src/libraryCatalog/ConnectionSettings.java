package libraryCatalog;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.regex.Pattern;

// How to reach the user's database. Saved in the user's own home folder (never in the project),
// so credentials can't end up in the code or in a git repository.
public class ConnectionSettings {

    public String dbHost = "localhost";
    public int dbPort = 3306;
    public String dbName = "";
    public String dbUser = "";
    public String dbPassword = "";

    public boolean sshEnabled = false;
    public String sshHost = "";
    public int sshPort = 22;
    public String sshUser = "";
    public String sshKeyFile = "";
    public String sshPassphrase = "";
    public String sshPassword = "";

    // When false, passwords are kept in memory only and asked for again at each start.
    public boolean rememberPasswords = true;

    private static final Pattern HOST = Pattern.compile("[A-Za-z0-9._:\\[\\]-]{1,255}");
    private static final Pattern DATABASE_NAME = Pattern.compile("[A-Za-z0-9_$-]{1,64}");

    public static Path file() {
        return Paths.get(System.getProperty("user.home"), ".relicroute", "connection.properties");
    }

    // Returns the saved settings, or null if nothing has been saved yet (or the file can't be read).
    public static ConnectionSettings load() {
        Path file = file();
        if (!Files.isRegularFile(file)) {
            return null;
        }
        Properties p = new Properties();
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ConnectionSettings s = new ConnectionSettings();
        s.dbHost = p.getProperty("db.host", s.dbHost);
        s.dbPort = intOr(p.getProperty("db.port"), s.dbPort);
        s.dbName = p.getProperty("db.name", "");
        s.dbUser = p.getProperty("db.user", "");
        s.dbPassword = p.getProperty("db.password", "");
        s.sshEnabled = Boolean.parseBoolean(p.getProperty("ssh.enabled", "false"));
        s.sshHost = p.getProperty("ssh.host", "");
        s.sshPort = intOr(p.getProperty("ssh.port"), s.sshPort);
        s.sshUser = p.getProperty("ssh.user", "");
        s.sshKeyFile = p.getProperty("ssh.keyfile", "");
        s.sshPassphrase = p.getProperty("ssh.passphrase", "");
        s.sshPassword = p.getProperty("ssh.password", "");
        s.rememberPasswords = Boolean.parseBoolean(p.getProperty("remember.passwords", "true"));
        return s;
    }

    // Writes the settings. Passwords are only written when rememberPasswords is on.
    public void save() throws IOException {
        Properties p = new Properties();
        p.setProperty("db.host", dbHost);
        p.setProperty("db.port", String.valueOf(dbPort));
        p.setProperty("db.name", dbName);
        p.setProperty("db.user", dbUser);
        p.setProperty("ssh.enabled", String.valueOf(sshEnabled));
        p.setProperty("ssh.host", sshHost);
        p.setProperty("ssh.port", String.valueOf(sshPort));
        p.setProperty("ssh.user", sshUser);
        p.setProperty("ssh.keyfile", sshKeyFile);
        p.setProperty("remember.passwords", String.valueOf(rememberPasswords));
        if (rememberPasswords) {
            p.setProperty("db.password", dbPassword);
            p.setProperty("ssh.passphrase", sshPassphrase);
            p.setProperty("ssh.password", sshPassword);
        }

        Path file = file();
        Files.createDirectories(file.getParent());
        boolean posix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
        if (posix) {
            Files.setPosixFilePermissions(file.getParent(), PosixFilePermissions.fromString("rwx------"));
            if (!Files.exists(file)) {
                Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            } else {
                Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
            }
        }
        try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            p.store(out, "The Relic Route database connection. Only you should be able to read this file.");
        }
    }

    public ConnectionSettings copy() {
        ConnectionSettings c = new ConnectionSettings();
        c.dbHost = dbHost;
        c.dbPort = dbPort;
        c.dbName = dbName;
        c.dbUser = dbUser;
        c.dbPassword = dbPassword;
        c.sshEnabled = sshEnabled;
        c.sshHost = sshHost;
        c.sshPort = sshPort;
        c.sshUser = sshUser;
        c.sshKeyFile = sshKeyFile;
        c.sshPassphrase = sshPassphrase;
        c.sshPassword = sshPassword;
        c.rememberPasswords = rememberPasswords;
        return c;
    }

    // Returns a message describing the first problem, or null if the settings can be used.
    // These values end up in a connection URL, so they are restricted to safe characters.
    public String validate() {
        if (!HOST.matcher(dbHost.trim()).matches()) {
            return "Please enter the database host (for example localhost).";
        }
        if (dbPort < 1 || dbPort > 65535) {
            return "The database port must be a number between 1 and 65535.";
        }
        if (!DATABASE_NAME.matcher(dbName.trim()).matches()) {
            return "Please enter the database name (letters, numbers, _ $ and - only).";
        }
        if (dbUser.trim().isEmpty()) {
            return "Please enter the database username.";
        }
        if (sshEnabled) {
            if (!HOST.matcher(sshHost.trim()).matches()) {
                return "Please enter the SSH host.";
            }
            if (sshPort < 1 || sshPort > 65535) {
                return "The SSH port must be a number between 1 and 65535.";
            }
            if (sshUser.trim().isEmpty()) {
                return "Please enter the SSH username.";
            }
            if (sshKeyFile.trim().isEmpty() && sshPassword.isEmpty()) {
                return "For the SSH tunnel, choose a private key file or enter an SSH password.";
            }
        }
        return null;
    }

    private static int intOr(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
