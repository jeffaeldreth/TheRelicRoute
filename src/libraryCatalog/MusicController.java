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
import java.util.ResourceBundle;

public class MusicController implements Initializable {

    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;
    @FXML private Button searchWebButton;

    @FXML private TableView<Album> musicTableView;
    @FXML private TableColumn<Album, String> albumColumn;
    @FXML private TableColumn<Album, String> artistColumn;
    @FXML private TableColumn<Album, String> genreColumn;
    @FXML private TableColumn<Album, String> labelColumn;
    @FXML private TableColumn<Album, Integer> onHandColumn;
    @FXML private TableColumn<Album, Boolean> vinylColumn;
    @FXML private TableColumn<Album, Boolean> cdColumn;
    @FXML private TableColumn<Album, Boolean> cassetteColumn;
    @FXML private TableColumn<Album, Boolean> eightTrackColumn;
    @FXML private TableColumn<Album, String> locationColumn;
    @FXML private TableColumn<Album, String> conditionColumn;
    @FXML private TableColumn<Album, Double> valueColumn;

    @FXML private TextField albumField;
    @FXML private TextField artistField;
    @FXML private TextField genreField;
    @FXML private TextField labelField;
    @FXML private TextField onHandField;
    @FXML private TextField locationField;
    @FXML private TextField conditionField;
    @FXML private TextField valueField;
    @FXML private CheckBox vinylCheck;
    @FXML private CheckBox cdCheck;
    @FXML private CheckBox cassetteCheck;
    @FXML private CheckBox eightTrackCheck;

    private boolean tableReady = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("recordLabel"));
        onHandColumn.setCellValueFactory(new PropertyValueFactory<>("onHand"));
        vinylColumn.setCellValueFactory(new PropertyValueFactory<>("vinyl"));
        cdColumn.setCellValueFactory(new PropertyValueFactory<>("cd"));
        cassetteColumn.setCellValueFactory(new PropertyValueFactory<>("cassette"));
        eightTrackColumn.setCellValueFactory(new PropertyValueFactory<>("eightTrack"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("currentCondition"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("selectionValue"));

        useCheckmarks(vinylColumn);
        useCheckmarks(cdColumn);
        useCheckmarks(cassetteColumn);
        useCheckmarks(eightTrackColumn);

        searchFieldCombo.setItems(FXCollections.observableArrayList(
                "Album", "Artist", "Genre", "Record Label", "On Hand", "Format", "Location"));
        searchFieldCombo.setValue("Album");
    }

    // Called by the main controller each time a MUSIC tree item is selected.
    public void showCategory(String treeLabel) {
        String searchOption = treeLabel == null ? null : mapLeafToSearchOption(treeLabel);
        if (searchOption != null) {
            searchFieldCombo.setValue(searchOption);
            searchTextField.requestFocus();
        }

        loadMusic();
        setSearchWebButtonVisible(false);
    }

    // Called when the app is pointed at a different database.
    public void forgetTableCheck() {
        tableReady = false;
    }

    // True if the music table already exists. Never creates it, but does add the
    // On Hand column to a music table that was created before that column existed.
    public boolean tableExists() {
        if (tableReady) {
            return true;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = 'music'")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        stmt.execute("ALTER TABLE music ADD COLUMN IF NOT EXISTS on_hand INT");
                        tableReady = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableReady;
    }

    // Creates the music table the first time something is added.
    private void ensureTable() {
        if (tableExists()) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS music (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "album VARCHAR(150) NOT NULL, " +
                        "artist VARCHAR(100), " +
                        "genre VARCHAR(100), " +
                        "record_label VARCHAR(100), " +
                        "on_hand INT, " +
                        "vinyl BOOLEAN, " +
                        "cd BOOLEAN, " +
                        "cassette BOOLEAN, " +
                        "eight_track BOOLEAN, " +
                        "location VARCHAR(50), " +
                        "current_condition VARCHAR(50), " +
                        "selection_value DECIMAL(10,2), " +
                        "INDEX idx_album (album), " +
                        "INDEX idx_artist (artist), " +
                        "INDEX idx_genre (genre), " +
                        "INDEX idx_label (record_label), " +
                        "INDEX idx_location (location))");
            }
            tableReady = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mapLeafToSearchOption(String treeLabel) {
        switch (treeLabel) {
            case "ALBUM": return "Album";
            case "ARTIST": return "Artist";
            case "GENRE": return "Genre";
            case "RECORD LABEL": return "Record Label";
            case "ON HAND": return "On Hand";
            case "FORMAT": return "Format";
            case "LOCATION": return "Location";
            default: return null; // MUSIC itself, CONDITION (not searchable)
        }
    }

    private String mapSearchOptionToColumn(String searchOption) {
        switch (searchOption) {
            case "Artist": return "artist";
            case "Genre": return "genre";
            case "Record Label": return "record_label";
            case "On Hand": return "on_hand";
            case "Location": return "location";
            default: return "album";
        }
    }

    private String formatColumnFor(String term) {
        String t = term.toLowerCase();
        if (t.contains("vinyl") || t.equals("lp") || t.contains("record")) return "vinyl";
        if (t.equals("cd") || t.contains("compact")) return "cd";
        if (t.contains("cassette") || t.contains("tape")) return "cassette";
        if (t.contains("8") && t.contains("track")) return "eight_track";
        return null;
    }

    private void loadMusic() {
        ObservableList<Album> albums = FXCollections.observableArrayList();
        if (!tableExists()) {
            musicTableView.setItems(albums);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM music")) {
                fillAlbumsFromResultSet(rs, albums);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        musicTableView.setItems(albums);
    }

    public void searchMusic() {
        String searchOption = searchFieldCombo.getValue();
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();

        if (searchOption == null || term.isEmpty()) {
            loadMusic();
            setSearchWebButtonVisible(false);
            return;
        }

        ObservableList<Album> results = FXCollections.observableArrayList();
        if (!tableExists()) {
            musicTableView.setItems(results);
            setSearchWebButtonVisible(true);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = null;

            if (searchOption.equals("Format")) {
                String formatColumn = formatColumnFor(term);
                if (formatColumn != null) {
                    stmt = conn.prepareStatement("SELECT * FROM music WHERE " + formatColumn + " = 1");
                }
            } else if (searchOption.equals("On Hand")) {
                try {
                    int number = Integer.parseInt(term);
                    stmt = conn.prepareStatement("SELECT * FROM music WHERE on_hand = ?");
                    stmt.setInt(1, number);
                } catch (NumberFormatException e) {
                    // not a number, so nothing can match
                }
            } else {
                stmt = conn.prepareStatement("SELECT * FROM music WHERE " + mapSearchOptionToColumn(searchOption) + " LIKE ?");
                stmt.setString(1, "%" + term + "%");
            }

            if (stmt != null) {
                try {
                    fillAlbumsFromResultSet(stmt.executeQuery(), results);
                } finally {
                    stmt.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        musicTableView.setItems(results);
        setSearchWebButtonVisible(results.isEmpty());
    }

    public void showAllMusic() {
        searchTextField.clear();
        loadMusic();
        setSearchWebButtonVisible(false);
    }

    public void searchWeb() {
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
        if (term.isEmpty()) {
            return;
        }
        if ("Album".equals(searchFieldCombo.getValue())) {
            term = term + " album";
        }
        webView.Main.openWithSearch(term);
    }

    private void setSearchWebButtonVisible(boolean visible) {
        searchWebButton.setVisible(visible);
        searchWebButton.setManaged(visible);
    }

    private void fillAlbumsFromResultSet(ResultSet rs, ObservableList<Album> albums) throws Exception {
        while (rs.next()) {
            Album album = new Album();
            album.setId(rs.getInt("id"));
            album.setTitle(rs.getString("album"));
            album.setArtist(rs.getString("artist"));
            album.setGenre(rs.getString("genre"));
            album.setRecordLabel(rs.getString("record_label"));
            album.setOnHand(rs.getInt("on_hand"));
            album.setVinyl(rs.getBoolean("vinyl"));
            album.setCd(rs.getBoolean("cd"));
            album.setCassette(rs.getBoolean("cassette"));
            album.setEightTrack(rs.getBoolean("eight_track"));
            album.setLocation(rs.getString("location"));
            album.setCurrentCondition(rs.getString("current_condition"));
            album.setSelectionValue(rs.getDouble("selection_value"));
            albums.add(album);
        }
    }

    public void addAlbum() {
        String title = albumField.getText() == null ? "" : albumField.getText().trim();
        if (title.isEmpty()) {
            return;
        }
        ensureTable();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO music (album, artist, genre, record_label, on_hand, vinyl, cd, cassette, eight_track, " +
                    "location, current_condition, selection_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, artistField.getText());
                stmt.setString(3, genreField.getText());
                stmt.setString(4, labelField.getText());
                stmt.setInt(5, parseIntOrZero(onHandField.getText()));
                stmt.setBoolean(6, vinylCheck.isSelected());
                stmt.setBoolean(7, cdCheck.isSelected());
                stmt.setBoolean(8, cassetteCheck.isSelected());
                stmt.setBoolean(9, eightTrackCheck.isSelected());
                stmt.setString(10, locationField.getText());
                stmt.setString(11, conditionField.getText());
                stmt.setDouble(12, parseDoubleOrZero(valueField.getText()));
                stmt.executeUpdate();
            }

            clearForm();
            loadMusic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAlbum() {
        Album selected = musicTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM music WHERE id = ?")) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
            }
            loadMusic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        albumField.clear();
        artistField.clear();
        genreField.clear();
        labelField.clear();
        onHandField.clear();
        locationField.clear();
        conditionField.clear();
        valueField.clear();
        vinylCheck.setSelected(false);
        cdCheck.setSelected(false);
        cassetteCheck.setSelected(false);
        eightTrackCheck.setSelected(false);
    }

    private void useCheckmarks(TableColumn<Album, Boolean> column) {
        column.setCellFactory(col -> new TableCell<Album, Boolean>() {
            @Override
            protected void updateItem(Boolean owned, boolean empty) {
                super.updateItem(owned, empty);
                setText(!empty && Boolean.TRUE.equals(owned) ? "✓" : null);
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
