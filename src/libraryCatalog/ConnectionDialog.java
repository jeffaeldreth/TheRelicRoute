package libraryCatalog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

// The "where is my database?" screen: shown on first run and from File > Database Connection.
public class ConnectionDialog {

    private final TextField hostField = new TextField();
    private final TextField portField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField userField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final CheckBox sshCheck = new CheckBox("Connect through an SSH tunnel");
    private final TextField sshHostField = new TextField();
    private final TextField sshPortField = new TextField();
    private final TextField sshUserField = new TextField();
    private final TextField sshKeyField = new TextField();
    private final PasswordField sshPassphraseField = new PasswordField();
    private final PasswordField sshPasswordField = new PasswordField();

    private final CheckBox rememberCheck = new CheckBox("Remember passwords on this computer");

    private ConnectionDialog(ConnectionSettings initial) {
        hostField.setText(initial.dbHost);
        portField.setText(String.valueOf(initial.dbPort));
        nameField.setText(initial.dbName);
        userField.setText(initial.dbUser);
        passwordField.setText(initial.dbPassword);
        sshCheck.setSelected(initial.sshEnabled);
        sshHostField.setText(initial.sshHost);
        sshPortField.setText(String.valueOf(initial.sshPort));
        sshUserField.setText(initial.sshUser);
        sshKeyField.setText(initial.sshKeyFile);
        sshPassphraseField.setText(initial.sshPassphrase);
        sshPasswordField.setText(initial.sshPassword);
        rememberCheck.setSelected(initial.rememberPasswords);
    }

    // Returns the settings the user entered, or empty if they cancelled.
    // firstRun only changes the wording at the top.
    public static Optional<ConnectionSettings> show(Window owner, ConnectionSettings initial, boolean firstRun) {
        return new ConnectionDialog(initial).showDialog(owner, firstRun);
    }

    private Optional<ConnectionSettings> showDialog(Window owner, boolean firstRun) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Database Connection");
        dialog.setHeaderText(firstRun
                ? "Where is your database?\nThe Relic Route keeps your collection in a MariaDB database. Enter how to reach it."
                : "Database connection settings");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(ConnectionDialog.class.getResource("Style.css").toExternalForm());
        ButtonType saveType = new ButtonType("Save and Connect", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        pane.setPrefWidth(560);

        GridPane database = grid();
        addRow(database, 0, "Host:", hostField);
        addRow(database, 1, "Port:", portField);
        addRow(database, 2, "Database name:", nameField);
        addRow(database, 3, "Username:", userField);
        addRow(database, 4, "Password:", passwordField);
        portField.setPrefWidth(90);
        portField.setMaxWidth(90);

        GridPane ssh = grid();
        addRow(ssh, 0, "SSH host:", sshHostField);
        addRow(ssh, 1, "SSH port:", sshPortField);
        addRow(ssh, 2, "SSH username:", sshUserField);
        Button browse = new Button("Browse...");
        browse.setOnAction(e -> chooseKeyFile(pane));
        HBox keyRow = new HBox(8, sshKeyField, browse);
        HBox.setHgrow(sshKeyField, Priority.ALWAYS);
        addRow(ssh, 3, "Private key file:", keyRow);
        addRow(ssh, 4, "Key passphrase:", sshPassphraseField);
        addRow(ssh, 5, "or SSH password:", sshPasswordField);
        sshPortField.setPrefWidth(90);
        sshPortField.setMaxWidth(90);
        ssh.disableProperty().bind(sshCheck.selectedProperty().not());

        Label note = new Label("Passwords are saved in a file only you can read (in your home folder), "
                + "never inside the app's own folder. Untick the box below to be asked each time instead.");
        note.setWrapText(true);

        Label status = new Label();
        status.setWrapText(true);
        Button test = new Button("Test Connection");
        test.setOnAction(e -> runTest(test, status));
        HBox testRow = new HBox(10, test, status);
        testRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(status, Priority.ALWAYS);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #ff8a80;");
        error.setWrapText(true);

        VBox content = new VBox(10, database, sshCheck, ssh, rememberCheck, note, testRow, error);
        content.setPadding(new Insets(10, 0, 0, 0));
        pane.setContent(content);

        Button save = (Button) pane.lookupButton(saveType);
        save.addEventFilter(ActionEvent.ACTION, event -> {
            String problem = readSettings().validate();
            if (problem != null) {
                error.setText(problem);
                event.consume(); // keep the dialog open
            }
        });

        Platform.runLater(() -> (firstRun || passwordField.getText().isEmpty() ? hostField : nameField).requestFocus());

        Optional<ButtonType> choice = dialog.showAndWait();
        if (choice.isPresent() && choice.get() == saveType) {
            return Optional.of(readSettings());
        }
        return Optional.empty();
    }

    private void runTest(Button test, Label status) {
        ConnectionSettings entered = readSettings();
        String problem = entered.validate();
        if (problem != null) {
            status.setText(problem);
            return;
        }
        test.setDisable(true);
        status.setText("Testing...");
        Thread worker = new Thread(() -> {
            String result = DatabaseConnection.test(entered);
            Platform.runLater(() -> {
                status.setText(result == null ? "Connected successfully." : "Couldn't connect: " + result);
                test.setDisable(false);
            });
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void chooseKeyFile(DialogPane pane) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose your SSH private key");
        File sshFolder = new File(System.getProperty("user.home"), ".ssh");
        if (sshFolder.isDirectory()) {
            chooser.setInitialDirectory(sshFolder);
        }
        File chosen = chooser.showOpenDialog(pane.getScene().getWindow());
        if (chosen != null) {
            sshKeyField.setText(chosen.getAbsolutePath());
        }
    }

    private ConnectionSettings readSettings() {
        ConnectionSettings s = new ConnectionSettings();
        s.dbHost = hostField.getText().trim();
        s.dbPort = parsePort(portField.getText());
        s.dbName = nameField.getText().trim();
        s.dbUser = userField.getText().trim();
        s.dbPassword = passwordField.getText();
        s.sshEnabled = sshCheck.isSelected();
        s.sshHost = sshHostField.getText().trim();
        s.sshPort = parsePort(sshPortField.getText());
        s.sshUser = sshUserField.getText().trim();
        s.sshKeyFile = sshKeyField.getText().trim();
        s.sshPassphrase = sshPassphraseField.getText();
        s.sshPassword = sshPasswordField.getText();
        s.rememberPasswords = rememberCheck.isSelected();
        return s;
    }

    private static int parsePort(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return -1; // fails validation with a clear message
        }
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        return grid;
    }

    private static void addRow(GridPane grid, int row, String label, javafx.scene.Node field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }
}
