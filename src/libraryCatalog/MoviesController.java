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

public class MoviesController implements Initializable {

    @FXML private ComboBox<String> searchFieldCombo;
    @FXML private TextField searchTextField;
    @FXML private Button searchWebButton;

    @FXML private TableView<Movie> moviesTableView;
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, String> directorColumn;
    @FXML private TableColumn<Movie, String> actorsColumn;
    @FXML private TableColumn<Movie, String> genreColumn;
    @FXML private TableColumn<Movie, String> ratingColumn;
    @FXML private TableColumn<Movie, Integer> runTimeColumn;
    @FXML private TableColumn<Movie, Integer> onHandColumn;
    @FXML private TableColumn<Movie, Boolean> dvdColumn;
    @FXML private TableColumn<Movie, Boolean> blurayColumn;
    @FXML private TableColumn<Movie, Boolean> uhdColumn;
    @FXML private TableColumn<Movie, Boolean> vhsColumn;
    @FXML private TableColumn<Movie, String> locationColumn;
    @FXML private TableColumn<Movie, String> conditionColumn;
    @FXML private TableColumn<Movie, Double> valueColumn;

    @FXML private TextField titleField;
    @FXML private TextField directorField;
    @FXML private TextField actorsField;
    @FXML private TextField genreField;
    @FXML private ComboBox<String> ratingCombo;
    @FXML private TextField runTimeField;
    @FXML private TextField onHandField;
    @FXML private TextField locationField;
    @FXML private TextField conditionField;
    @FXML private TextField valueField;
    @FXML private CheckBox dvdCheck;
    @FXML private CheckBox blurayCheck;
    @FXML private CheckBox uhdCheck;
    @FXML private CheckBox vhsCheck;

    private boolean tableReady = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        directorColumn.setCellValueFactory(new PropertyValueFactory<>("director"));
        actorsColumn.setCellValueFactory(new PropertyValueFactory<>("leadingActors"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        runTimeColumn.setCellValueFactory(new PropertyValueFactory<>("runTime"));
        onHandColumn.setCellValueFactory(new PropertyValueFactory<>("onHand"));
        dvdColumn.setCellValueFactory(new PropertyValueFactory<>("dvd"));
        blurayColumn.setCellValueFactory(new PropertyValueFactory<>("bluray"));
        uhdColumn.setCellValueFactory(new PropertyValueFactory<>("uhd4k"));
        vhsColumn.setCellValueFactory(new PropertyValueFactory<>("vhs"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("currentCondition"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("selectionValue"));

        useCheckmarks(dvdColumn);
        useCheckmarks(blurayColumn);
        useCheckmarks(uhdColumn);
        useCheckmarks(vhsColumn);

        searchFieldCombo.setItems(FXCollections.observableArrayList(
                "Title", "Director", "Leading Actors", "Genre", "Rating", "Run Time", "On Hand", "Format", "Location"));
        searchFieldCombo.setValue("Title");

        ratingCombo.setItems(FXCollections.observableArrayList(
                "G", "PG", "PG-13", "R", "NC-17", "NR", "TV-G", "TV-PG", "TV-14", "TV-MA"));
    }

    // Called by the main controller each time a MOVIES tree item is selected.
    public void showCategory(String treeLabel) {
        String searchOption = treeLabel == null ? null : mapLeafToSearchOption(treeLabel);
        if (searchOption != null) {
            searchFieldCombo.setValue(searchOption);
            searchTextField.requestFocus();
        }

        loadMovies();
        setSearchWebButtonVisible(false);
    }

    // Called when the app is pointed at a different database.
    public void forgetTableCheck() {
        tableReady = false;
    }

    // True if the movies table already exists. Never creates it.
    public boolean tableExists() {
        if (tableReady) {
            return true;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables " +
                         "WHERE table_schema = DATABASE() AND table_name = 'movies'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tableReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableReady;
    }

    // Creates the movies table the first time something is added.
    private void ensureTable() {
        if (tableExists()) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS movies (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(150) NOT NULL, " +
                        "director VARCHAR(100), " +
                        "leading_actors VARCHAR(255), " +
                        "genre VARCHAR(100), " +
                        "rating VARCHAR(10), " +
                        "run_time INT, " +
                        "on_hand INT, " +
                        "dvd BOOLEAN, " +
                        "bluray BOOLEAN, " +
                        "uhd_4k BOOLEAN, " +
                        "vhs BOOLEAN, " +
                        "location VARCHAR(50), " +
                        "current_condition VARCHAR(50), " +
                        "selection_value DECIMAL(10,2), " +
                        "INDEX idx_title (title), " +
                        "INDEX idx_director (director), " +
                        "INDEX idx_genre (genre), " +
                        "INDEX idx_rating (rating), " +
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
            case "DIRECTOR": return "Director";
            case "LEADING ACTORS": return "Leading Actors";
            case "GENRE": return "Genre";
            case "RATING": return "Rating";
            case "RUN TIME": return "Run Time";
            case "ON HAND": return "On Hand";
            case "FORMAT": return "Format";
            case "LOCATION": return "Location";
            default: return null; // MOVIES itself, CONDITION (not searchable)
        }
    }

    private String mapSearchOptionToColumn(String searchOption) {
        switch (searchOption) {
            case "Director": return "director";
            case "Leading Actors": return "leading_actors";
            case "Genre": return "genre";
            case "Rating": return "rating";
            case "Run Time": return "run_time";
            case "On Hand": return "on_hand";
            case "Location": return "location";
            default: return "title";
        }
    }

    private String formatColumnFor(String term) {
        String t = term.toLowerCase();
        if (t.contains("dvd")) return "dvd";
        if (t.contains("blu")) return "bluray";
        if (t.contains("4k") || t.contains("uhd")) return "uhd_4k";
        if (t.contains("vhs")) return "vhs";
        return null;
    }

    private void loadMovies() {
        ObservableList<Movie> movies = FXCollections.observableArrayList();
        if (!tableExists()) {
            moviesTableView.setItems(movies);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM movies")) {
                fillMoviesFromResultSet(rs, movies);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        moviesTableView.setItems(movies);
    }

    public void searchMovies() {
        String searchOption = searchFieldCombo.getValue();
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();

        if (searchOption == null || term.isEmpty()) {
            loadMovies();
            setSearchWebButtonVisible(false);
            return;
        }

        ObservableList<Movie> results = FXCollections.observableArrayList();
        if (!tableExists()) {
            moviesTableView.setItems(results);
            setSearchWebButtonVisible(true);
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = null;

            if (searchOption.equals("Format")) {
                String formatColumn = formatColumnFor(term);
                if (formatColumn != null) {
                    stmt = conn.prepareStatement("SELECT * FROM movies WHERE " + formatColumn + " = 1");
                }
            } else if (searchOption.equals("Rating")) {
                stmt = conn.prepareStatement("SELECT * FROM movies WHERE rating = ?");
                stmt.setString(1, term);
            } else if (searchOption.equals("Run Time") || searchOption.equals("On Hand")) {
                try {
                    int number = Integer.parseInt(term);
                    stmt = conn.prepareStatement("SELECT * FROM movies WHERE " + mapSearchOptionToColumn(searchOption) + " = ?");
                    stmt.setInt(1, number);
                } catch (NumberFormatException e) {
                    // not a number, so nothing can match
                }
            } else {
                stmt = conn.prepareStatement("SELECT * FROM movies WHERE " + mapSearchOptionToColumn(searchOption) + " LIKE ?");
                stmt.setString(1, "%" + term + "%");
            }

            if (stmt != null) {
                try {
                    fillMoviesFromResultSet(stmt.executeQuery(), results);
                } finally {
                    stmt.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        moviesTableView.setItems(results);
        setSearchWebButtonVisible(results.isEmpty());
    }

    public void showAllMovies() {
        searchTextField.clear();
        loadMovies();
        setSearchWebButtonVisible(false);
    }

    public void searchWeb() {
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
        if (term.isEmpty()) {
            return;
        }
        if ("Title".equals(searchFieldCombo.getValue())) {
            term = term + " movie";
        }
        webView.Main.openWithSearch(term);
    }

    private void setSearchWebButtonVisible(boolean visible) {
        searchWebButton.setVisible(visible);
        searchWebButton.setManaged(visible);
    }

    private void fillMoviesFromResultSet(ResultSet rs, ObservableList<Movie> movies) throws Exception {
        while (rs.next()) {
            Movie movie = new Movie();
            movie.setId(rs.getInt("id"));
            movie.setTitle(rs.getString("title"));
            movie.setDirector(rs.getString("director"));
            movie.setLeadingActors(rs.getString("leading_actors"));
            movie.setGenre(rs.getString("genre"));
            movie.setRating(rs.getString("rating"));
            movie.setRunTime(rs.getInt("run_time"));
            movie.setOnHand(rs.getInt("on_hand"));
            movie.setDvd(rs.getBoolean("dvd"));
            movie.setBluray(rs.getBoolean("bluray"));
            movie.setUhd4k(rs.getBoolean("uhd_4k"));
            movie.setVhs(rs.getBoolean("vhs"));
            movie.setLocation(rs.getString("location"));
            movie.setCurrentCondition(rs.getString("current_condition"));
            movie.setSelectionValue(rs.getDouble("selection_value"));
            movies.add(movie);
        }
    }

    public void addMovie() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            return;
        }
        ensureTable();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO movies (title, director, leading_actors, genre, rating, run_time, on_hand, " +
                    "dvd, bluray, uhd_4k, vhs, location, current_condition, selection_value) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, directorField.getText());
                stmt.setString(3, actorsField.getText());
                stmt.setString(4, genreField.getText());
                stmt.setString(5, ratingCombo.getValue());
                stmt.setInt(6, parseIntOrZero(runTimeField.getText()));
                stmt.setInt(7, parseIntOrZero(onHandField.getText()));
                stmt.setBoolean(8, dvdCheck.isSelected());
                stmt.setBoolean(9, blurayCheck.isSelected());
                stmt.setBoolean(10, uhdCheck.isSelected());
                stmt.setBoolean(11, vhsCheck.isSelected());
                stmt.setString(12, locationField.getText());
                stmt.setString(13, conditionField.getText());
                stmt.setDouble(14, parseDoubleOrZero(valueField.getText()));
                stmt.executeUpdate();
            }

            clearForm();
            loadMovies();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMovie() {
        Movie selected = moviesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM movies WHERE id = ?")) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
            }
            loadMovies();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.clear();
        directorField.clear();
        actorsField.clear();
        genreField.clear();
        ratingCombo.setValue(null);
        runTimeField.clear();
        onHandField.clear();
        locationField.clear();
        conditionField.clear();
        valueField.clear();
        dvdCheck.setSelected(false);
        blurayCheck.setSelected(false);
        uhdCheck.setSelected(false);
        vhsCheck.setSelected(false);
    }

    private void useCheckmarks(TableColumn<Movie, Boolean> column) {
        column.setCellFactory(col -> new TableCell<Movie, Boolean>() {
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
