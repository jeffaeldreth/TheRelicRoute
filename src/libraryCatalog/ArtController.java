package libraryCatalog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ResourceBundle;

public class ArtController implements Initializable {

    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;
    @FXML private Button searchWebButton;

    @FXML private TableView<Artwork> artTableView;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;
    @FXML private TableColumn<Artwork, String> mediumColumn;
    @FXML private TableColumn<Artwork, String> genreColumn;
    @FXML private TableColumn<Artwork, Integer> yearColumn;
    @FXML private TableColumn<Artwork, String> dimensionsColumn;
    @FXML private TableColumn<Artwork, Boolean> framedColumn;
    @FXML private TableColumn<Artwork, Boolean> signedColumn;
    @FXML private TableColumn<Artwork, Integer> onHandColumn;
    @FXML private TableColumn<Artwork, String> purchasedColumn;
    @FXML private TableColumn<Artwork, String> locationColumn;
    @FXML private TableColumn<Artwork, String> conditionColumn;
    @FXML private TableColumn<Artwork, Double> valueColumn;

    @FXML private TextField titleField;
    @FXML private TextField artistField;
    @FXML private TextField mediumField;
    @FXML private TextField genreField;
    @FXML private TextField yearField;
    @FXML private TextField dimensionsField;
    @FXML private TextField onHandField;
    @FXML private TextField purchasedField;
    @FXML private TextField locationField;
    @FXML private TextField conditionField;
    @FXML private TextField valueField;
    @FXML private CheckBox framedCheck;
    @FXML private CheckBox signedCheck;

    private boolean tableReady = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        mediumColumn.setCellValueFactory(new PropertyValueFactory<>("medium"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearMade"));
        dimensionsColumn.setCellValueFactory(new PropertyValueFactory<>("dimensions"));
        framedColumn.setCellValueFactory(new PropertyValueFactory<>("framed"));
        signedColumn.setCellValueFactory(new PropertyValueFactory<>("signed"));
        onHandColumn.setCellValueFactory(new PropertyValueFactory<>("onHand"));
        purchasedColumn.setCellValueFactory(new PropertyValueFactory<>("purchasedFrom"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("currentCondition"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("selectionValue"));

        useCheckmarks(framedColumn);
        useCheckmarks(signedColumn);

        // A year that was left blank is stored as empty, so show nothing rather than 0.
        yearColumn.setCellFactory(col -> new TableCell<Artwork, Integer>() {
            @Override
            protected void updateItem(Integer year, boolean empty) {
                super.updateItem(year, empty);
                setText(empty || year == null || year == 0 ? null : year.toString());
            }
        });

        searchFieldCombo.setItems(FXCollections.observableArrayList(
                "Title", "Artist", "Medium", "Genre", "Year", "Dimensions", "Framed", "Signed",
                "On Hand", "Purchased From", "Location"));
        searchFieldCombo.setValue("Title");
    }

    // Called by the main controller each time an ART tree item is selected.
    public void showCategory(String treeLabel) {
        String searchOption = treeLabel == null ? null : mapLeafToSearchOption(treeLabel);
        if (searchOption != null) {
            searchFieldCombo.setValue(searchOption);
            searchTextField.requestFocus();
        }

        loadArt();
        setSearchWebButtonVisible(false);
    }

    // Called when the app is pointed at a different database.
    public void forgetTableCheck() {
        tableReady = false;
    }

    // True if the art table already exists. Never creates it.
    public boolean tableExists() {
        if (tableReady) {
            return true;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables " +
                         "WHERE table_schema = DATABASE() AND table_name = 'art'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableReady;
    }

    // Creates the art table the first time something is added.
    private void ensureTable() {
        if (tableExists()) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS art (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(150) NOT NULL, " +
                        "artist VARCHAR(100), " +
                        "medium VARCHAR(100), " +
                        "genre VARCHAR(100), " +
                        "year_made INT, " +
                        "dimensions VARCHAR(50), " +
                        "framed BOOLEAN, " +
                        "signed BOOLEAN, " +
                        "on_hand INT, " +
                        "purchased_from VARCHAR(150), " +
                        "location VARCHAR(50), " +
                        "current_condition VARCHAR(50), " +
                        "selection_value DECIMAL(10,2), " +
                        "INDEX idx_title (title), " +
                        "INDEX idx_artist (artist), " +
                        "INDEX idx_medium (medium), " +
                        "INDEX idx_genre (genre), " +
                        "INDEX idx_location (location))");
            }
            tableReady = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mapLeafToSearchOption(String treeLabel) {
        switch (treeLabel) {
            case "TITLE": return "Title";
            case "ARTIST": return "Artist";
            case "MEDIUM": return "Medium";
            case "GENRE": return "Genre";
            case "YEAR": return "Year";
            case "DIMENSIONS": return "Dimensions";
            case "FRAMED": return "Framed";
            case "SIGNED": return "Signed";
            case "ON HAND": return "On Hand";
            case "PURCHASED FROM": return "Purchased From";
            case "LOCATION": return "Location";
            default: return null; // ART itself, CONDITION (not searchable)
        }
    }

    private String mapSearchOptionToColumn(String searchOption) {
        switch (searchOption) {
            case "Artist": return "artist";
            case "Medium": return "medium";
            case "Genre": return "genre";
            case "Year": return "year_made";
            case "Dimensions": return "dimensions";
            case "Framed": return "framed";
            case "Signed": return "signed";
            case "On Hand": return "on_hand";
            case "Purchased From": return "purchased_from";
            case "Location": return "location";
            default: return "title";
        }
    }

    private void loadArt() {
        ObservableList<Artwork> pieces = FXCollections.observableArrayList();
        if (!tableExists()) {
            artTableView.setItems(pieces);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM art")) {
                fillArtFromResultSet(rs, pieces);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        artTableView.setItems(pieces);
    }

    public void searchArt() {
        String searchOption = searchFieldCombo.getValue();
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();

        if (searchOption == null || term.isEmpty()) {
            loadArt();
            setSearchWebButtonVisible(false);
            return;
        }

        ObservableList<Artwork> results = FXCollections.observableArrayList();
        if (!tableExists()) {
            artTableView.setItems(results);
            setSearchWebButtonVisible(true);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            String column = mapSearchOptionToColumn(searchOption);
            PreparedStatement stmt = null;

            if (searchOption.equals("Framed") || searchOption.equals("Signed")) {
                boolean yes = term.equalsIgnoreCase("yes") || term.equalsIgnoreCase("true") || term.equals("1");
                stmt = conn.prepareStatement("SELECT * FROM art WHERE " + column + " = ?");
                stmt.setBoolean(1, yes);
            } else if (searchOption.equals("Year") || searchOption.equals("On Hand")) {
                try {
                    int number = Integer.parseInt(term);
                    stmt = conn.prepareStatement("SELECT * FROM art WHERE " + column + " = ?");
                    stmt.setInt(1, number);
                } catch (NumberFormatException e) {
                    // not a number, so nothing can match
                }
            } else {
                stmt = conn.prepareStatement("SELECT * FROM art WHERE " + column + " LIKE ?");
                stmt.setString(1, "%" + term + "%");
            }

            if (stmt != null) {
                try {
                    fillArtFromResultSet(stmt.executeQuery(), results);
                } finally {
                    stmt.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        artTableView.setItems(results);
        setSearchWebButtonVisible(results.isEmpty());
    }

    public void showAllArt() {
        searchTextField.clear();
        loadArt();
        setSearchWebButtonVisible(false);
    }

    public void searchWeb() {
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
        if (term.isEmpty()) {
            return;
        }
        if ("Title".equals(searchFieldCombo.getValue())) {
            term = term + " artwork";
        }
        webView.Main.openWithSearch(term);
    }

    private void setSearchWebButtonVisible(boolean visible) {
        searchWebButton.setVisible(visible);
        searchWebButton.setManaged(visible);
    }

    private void fillArtFromResultSet(ResultSet rs, ObservableList<Artwork> pieces) throws Exception {
        while (rs.next()) {
            Artwork art = new Artwork();
            art.setId(rs.getInt("id"));
            art.setTitle(rs.getString("title"));
            art.setArtist(rs.getString("artist"));
            art.setMedium(rs.getString("medium"));
            art.setGenre(rs.getString("genre"));
            art.setYearMade(rs.getInt("year_made"));
            art.setDimensions(rs.getString("dimensions"));
            art.setFramed(rs.getBoolean("framed"));
            art.setSigned(rs.getBoolean("signed"));
            art.setOnHand(rs.getInt("on_hand"));
            art.setPurchasedFrom(rs.getString("purchased_from"));
            art.setLocation(rs.getString("location"));
            art.setCurrentCondition(rs.getString("current_condition"));
            art.setSelectionValue(rs.getDouble("selection_value"));
            pieces.add(art);
        }
    }

    public void addArt() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            return;
        }
        ensureTable();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO art (title, artist, medium, genre, year_made, dimensions, framed, signed, " +
                    "on_hand, purchased_from, location, current_condition, selection_value) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, artistField.getText());
                stmt.setString(3, mediumField.getText());
                stmt.setString(4, genreField.getText());
                try {
                    stmt.setInt(5, Integer.parseInt(yearField.getText().trim()));
                } catch (Exception e) {
                    stmt.setNull(5, Types.INTEGER);
                }
                stmt.setString(6, dimensionsField.getText());
                stmt.setBoolean(7, framedCheck.isSelected());
                stmt.setBoolean(8, signedCheck.isSelected());
                stmt.setInt(9, parseIntOrZero(onHandField.getText()));
                stmt.setString(10, purchasedField.getText());
                stmt.setString(11, locationField.getText());
                stmt.setString(12, conditionField.getText());
                stmt.setDouble(13, parseDoubleOrZero(valueField.getText()));
                stmt.executeUpdate();
            }

            clearForm();
            loadArt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteArt() {
        Artwork selected = artTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM art WHERE id = ?")) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
            }
            loadArt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.clear();
        artistField.clear();
        mediumField.clear();
        genreField.clear();
        yearField.clear();
        dimensionsField.clear();
        onHandField.clear();
        purchasedField.clear();
        locationField.clear();
        conditionField.clear();
        valueField.clear();
        framedCheck.setSelected(false);
        signedCheck.setSelected(false);
    }

    private void useCheckmarks(TableColumn<Artwork, Boolean> column) {
        column.setCellFactory(col -> new TableCell<Artwork, Boolean>() {
            @Override
            protected void updateItem(Boolean yes, boolean empty) {
                super.updateItem(yes, empty);
                setText(!empty && Boolean.TRUE.equals(yes) ? "✓" : null);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    private int parseIntOrZero(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
