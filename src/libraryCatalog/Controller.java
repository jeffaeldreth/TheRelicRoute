package libraryCatalog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

        @FXML
        private TreeView<String> libraryTreeView;

        @FXML
        private VBox contentPane;
        @FXML
        private javafx.scene.image.ImageView logoImageView;

        @FXML
        private VBox moviesPane;
        @FXML
        private MoviesController moviesPaneController;
        @FXML
        private VBox musicPane;
        @FXML
        private MusicController musicPaneController;
        @FXML
        private VBox artPane;
        @FXML
        private ArtController artPaneController;

        @FXML
        private StackPane centerStack;

        private TreeItem<String> rootItem;
        // User-made categories, keyed by their tree label (the name in upper case).
        private final Map<String, CustomCategoryPane> customPanes = new LinkedHashMap<>();
        private final Map<String, TreeItem<String>> customBranches = new LinkedHashMap<>();

        @FXML
        private ComboBox<String> searchFieldCombo;
        @FXML
        private TextField searchTextField;
        @FXML
        private javafx.scene.control.Button searchWebButton;

        @FXML
        private TableView<Book> booksTableView;
        @FXML
        private TableColumn<Book, String> titleColumn;
        @FXML
        private TableColumn<Book, String> authorColumn;
        @FXML
        private TableColumn<Book, String> genreColumn;
        @FXML
        private TableColumn<Book, Integer> onHandColumn;
        @FXML
        private TableColumn<Book, Boolean> hardbackColumn;
        @FXML
        private TableColumn<Book, Boolean> paperbackColumn;
        @FXML
        private TableColumn<Book, String> locationColumn;
        @FXML
        private TableColumn<Book, String> conditionColumn;
        @FXML
        private TableColumn<Book, Integer> pagesColumn;
        @FXML
        private TableColumn<Book, Double> valueColumn;

        @FXML
        private TextField titleField;
        @FXML
        private TextField authorField;
        @FXML
        private TextField genreField;
        @FXML
        private TextField onHandField;
        @FXML
        private CheckBox hardbackCheck;
        @FXML
        private CheckBox paperbackCheck;
        @FXML
        private TextField locationField;
        @FXML
        private TextField conditionField;
        @FXML
        private TextField pagesField;
        @FXML
        private TextField valueField;

        @Override
        public void initialize(URL arg0, ResourceBundle arg1) {

            rootItem = new TreeItem<>("MEDIATYPE",
                    new ImageView(new Image(getClass().getResourceAsStream("menu.png"))));

            TreeItem<String> branchItem1 = new TreeItem<>("BOOKS",
                    new ImageView(new Image(getClass().getResourceAsStream("books.png"))));

            TreeItem<String> leafItem1 = new TreeItem<>("GENRE",
                    new ImageView(new Image(getClass().getResourceAsStream("genre.png"))));
            TreeItem<String> leafItem2 = new TreeItem<>("AUTHOR",
                    new ImageView(new Image(getClass().getResourceAsStream("author.png"))));
            TreeItem<String> leafItem3 = new TreeItem<>("TITLE",
                    new ImageView(new Image(getClass().getResourceAsStream("title.png"))));
            TreeItem<String> leafItem4 = new TreeItem<>("ON HAND",
                new ImageView(new Image(getClass().getResourceAsStream("onhand.png"))));
            TreeItem<String> leafItem5 = new TreeItem<>("CONDITION",
                    new ImageView(new Image(getClass().getResourceAsStream("condition.png"))));
            TreeItem<String> leafItem6 = new TreeItem<>("LOCATION",
                    new ImageView(new Image(getClass().getResourceAsStream("location.png"))));
            TreeItem<String> leafItem7 = new TreeItem<>("HARDBACK",
                    new ImageView(new Image(getClass().getResourceAsStream("hardback.png"))));
            TreeItem<String> leafItem8 = new TreeItem<>("PAPERBACK",
                    new ImageView(new Image(getClass().getResourceAsStream("paperback.png"))));

            branchItem1.getChildren().addAll(leafItem1, leafItem2, leafItem3, leafItem4, leafItem5, leafItem6, leafItem7, leafItem8);

            TreeItem<String> branchItem2 = new TreeItem<>("MOVIES",
                    new ImageView(new Image(getClass().getResourceAsStream("movies.png"))));
            TreeItem<String> branchItem3 = new TreeItem<>("MUSIC",
                    new ImageView(new Image(getClass().getResourceAsStream("music.png"))));
            TreeItem<String> branchItem4 = new TreeItem<>("ART",
                    new ImageView(new Image(getClass().getResourceAsStream("art.png"))));

            branchItem2.getChildren().addAll(
                    leaf("TITLE", "title.png"),
                    leaf("DIRECTOR", "director.png"),
                    leaf("LEADING ACTORS", "actors.png"),
                    leaf("GENRE", "genre.png"),
                    leaf("RATING", "rating.png"),
                    leaf("RUN TIME", "runtime.png"),
                    leaf("FORMAT", "format.png"),
                    leaf("ON HAND", "onhand.png"),
                    leaf("LOCATION", "location.png"),
                    leaf("CONDITION", "condition.png"));

            branchItem3.getChildren().addAll(
                    leaf("ALBUM", "album.png"),
                    leaf("ARTIST", "artist.png"),
                    leaf("GENRE", "genre.png"),
                    leaf("RECORD LABEL", "label.png"),
                    leaf("ON HAND", "onhand.png"),
                    leaf("FORMAT", "format.png"),
                    leaf("LOCATION", "location.png"),
                    leaf("CONDITION", "condition.png"));

            branchItem4.getChildren().addAll(
                    leaf("TITLE", "title.png"),
                    leaf("ARTIST", "author.png"),
                    leaf("MEDIUM", "medium.png"),
                    leaf("GENRE", "genre.png"),
                    leaf("YEAR", "year.png"),
                    leaf("DIMENSIONS", "dimensions.png"),
                    leaf("FRAMED", "framed.png"),
                    leaf("SIGNED", "signed.png"),
                    leaf("ON HAND", "onhand.png"),
                    leaf("PURCHASED FROM", "purchased.png"),
                    leaf("LOCATION", "location.png"),
                    leaf("CONDITION", "condition.png"));

            rootItem.getChildren().addAll(branchItem1, branchItem2, branchItem3, branchItem4);

            rootItem.setExpanded(true);

            libraryTreeView.setRoot(rootItem);
            libraryTreeView.setShowRoot(true);

            libraryTreeView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldItem, newItem) -> handleTreeSelection(newItem));

            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
            genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
            onHandColumn.setCellValueFactory(new PropertyValueFactory<>("onHand"));
            hardbackColumn.setCellValueFactory(new PropertyValueFactory<>("hardback"));
            paperbackColumn.setCellValueFactory(new PropertyValueFactory<>("paperback"));
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            conditionColumn.setCellValueFactory(new PropertyValueFactory<>("currentCondition"));
            pagesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfPages"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("selectionValue"));

            searchFieldCombo.setItems(FXCollections.observableArrayList(
                    "Title", "Author", "Genre", "On Hand", "Location", "Hardback", "Paperback"));
            searchFieldCombo.setValue("Title");
        }

        private TreeItem<String> leaf(String label, String iconFile) {
            return new TreeItem<>(label, new ImageView(new Image(getClass().getResourceAsStream(iconFile))));
        }

        // Called once by Main after the window is visible: connects to the database, asking for the
        // connection details first if they haven't been saved yet.
        public void onWindowShown() {
            DatabaseConnection.setErrorReporter(this::reportConnectionProblem);

            ConnectionSettings saved = ConnectionSettings.load();
            if (saved != null && saved.rememberPasswords && saved.validate() == null) {
                applyConnection(saved);
                return;
            }

            // First run, or the user chose not to save passwords: ask.
            Optional<ConnectionSettings> chosen = ConnectionDialog.show(
                    libraryTreeView.getScene().getWindow(), saved != null ? saved : new ConnectionSettings(), saved == null);
            chosen.ifPresent(this::saveAndApply);
        }

        public void showConnectionSettings() {
            ConnectionSettings current = DatabaseConnection.getSettings();
            if (current == null) {
                current = ConnectionSettings.load();
            }
            if (current == null) {
                current = new ConnectionSettings();
            }
            ConnectionDialog.show(libraryTreeView.getScene().getWindow(), current, false).ifPresent(this::saveAndApply);
        }

        private void saveAndApply(ConnectionSettings settings) {
            try {
                settings.save();
            } catch (IOException e) {
                e.printStackTrace();
                showMessage(Alert.AlertType.ERROR, "Your settings couldn't be saved, so you'll be asked again next time:\n" + e.getMessage());
            }
            applyConnection(settings);
        }

        private void applyConnection(ConnectionSettings settings) {
            DatabaseConnection.configure(settings);
            resetForNewDatabase();
            loadCustomCategories();
        }

        // Forgets everything learned from the previous database, so nothing stale is shown.
        private void resetForNewDatabase() {
            libraryTreeView.getSelectionModel().clearSelection();
            showPane(null);

            booksTableReady = false;
            moviesPaneController.forgetTableCheck();
            musicPaneController.forgetTableCheck();
            artPaneController.forgetTableCheck();

            for (String label : new ArrayList<>(customPanes.keySet())) {
                centerStack.getChildren().remove(customPanes.remove(label));
                rootItem.getChildren().remove(customBranches.remove(label));
            }
        }

        private boolean connectionAlertShowing = false;

        private void reportConnectionProblem(String message) {
            if (connectionAlertShowing) {
                return;
            }
            connectionAlertShowing = true;
            try {
                showMessage(Alert.AlertType.ERROR, "Couldn't connect to the database.\n\n" + message
                        + "\n\nYou can check your settings under File > Database Connection...");
            } finally {
                connectionAlertShowing = false;
            }
        }

        // Adds a tree branch and pane for every category the user has saved in this database.
        private void loadCustomCategories() {
            try {
                for (CustomCategories.Category category : CustomCategories.loadAll()) {
                    addCustomCategoryToUi(category);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Adds the category's pane and its tree branch, and returns the new branch.
        private TreeItem<String> addCustomCategoryToUi(CustomCategories.Category category) {
            String label = category.name.toUpperCase();

            CustomCategoryPane pane = new CustomCategoryPane(category);
            centerStack.getChildren().add(pane);
            customPanes.put(label, pane);

            TreeItem<String> branch = leaf(label, category.icon);
            branch.getChildren().add(leaf("TITLE", "title.png"));
            for (CustomCategories.Field field : category.fields) {
                branch.getChildren().add(leaf(field.name.toUpperCase(), CustomCategories.fieldIconFile(field.type)));
            }
            branch.getChildren().add(leaf("ON HAND", "onhand.png"));
            branch.getChildren().add(leaf("LOCATION", "location.png"));
            branch.getChildren().add(leaf("CONDITION", "condition.png"));

            rootItem.getChildren().add(branch);
            customBranches.put(label, branch);
            return branch;
        }

        public void newCategory() {
            Optional<NewCategoryDialog.Result> result = NewCategoryDialog.show(
                    libraryTreeView.getScene().getWindow(), new HashSet<>(customPanes.keySet()));
            if (result.isEmpty()) {
                return;
            }
            NewCategoryDialog.Result request = result.get();
            try {
                CustomCategories.Category created = CustomCategories.create(
                        request.name, request.icon, request.fieldNames, request.fieldTypes);
                TreeItem<String> branch = addCustomCategoryToUi(created);
                libraryTreeView.getSelectionModel().select(branch);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage(Alert.AlertType.ERROR, "Couldn't create the category:\n" + e.getMessage());
            }
        }

        public void removeCategory() {
            if (customPanes.isEmpty()) {
                showMessage(Alert.AlertType.INFORMATION, "You haven't created any custom categories yet.");
                return;
            }

            ChoiceDialog<String> chooser = new ChoiceDialog<>(customPanes.keySet().iterator().next(), customPanes.keySet());
            chooser.initOwner(libraryTreeView.getScene().getWindow());
            chooser.setTitle("Remove Category");
            chooser.setHeaderText("Which category do you want to remove?");
            chooser.setContentText("Category:");
            chooser.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
            Optional<String> choice = chooser.showAndWait();
            if (choice.isEmpty()) {
                return;
            }

            String label = choice.get();
            CustomCategoryPane pane = customPanes.get(label);
            int count = pane.countRows();

            ButtonType deleteType = new ButtonType("Delete Everything", ButtonBar.ButtonData.OK_DONE);
            Alert confirm = new Alert(Alert.AlertType.WARNING,
                    "This permanently deletes the category \"" + pane.getCategory().name + "\" and all "
                            + count + " item(s) in it.\n\nThis cannot be undone.",
                    deleteType, ButtonType.CANCEL);
            confirm.initOwner(libraryTreeView.getScene().getWindow());
            confirm.setTitle("Remove Category");
            confirm.setHeaderText("Delete " + pane.getCategory().name + "?");
            confirm.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
            ((Button) confirm.getDialogPane().lookupButton(deleteType)).setDefaultButton(false);
            ((Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
            Optional<ButtonType> answer = confirm.showAndWait();
            if (answer.isEmpty() || answer.get() != deleteType) {
                return;
            }

            try {
                CustomCategories.delete(pane.getCategory());
                centerStack.getChildren().remove(pane);
                customPanes.remove(label);
                rootItem.getChildren().remove(customBranches.remove(label));
                libraryTreeView.getSelectionModel().select(rootItem);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage(Alert.AlertType.ERROR, "Couldn't remove the category:\n" + e.getMessage());
            }
        }

        private void showMessage(Alert.AlertType type, String text) {
            Alert alert = new Alert(type, text);
            alert.initOwner(libraryTreeView.getScene().getWindow());
            alert.setHeaderText(null);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
            alert.showAndWait();
        }

        private void handleTreeSelection(TreeItem<String> item) {
            if (item == null) {
                return;
            }

            // The root's children (BOOKS, MOVIES, ...) are the media branches; anything deeper is a category leaf.
            boolean isLeaf = item.getParent() != null && item.getParent().getParent() != null;
            TreeItem<String> branch = isLeaf ? item.getParent() : item;
            String leafLabel = isLeaf ? item.getValue() : null;

            switch (branch.getValue()) {
                case "BOOKS":
                    showPane(contentPane);
                    String searchOption = leafLabel == null ? null : mapLeafToSearchOption(leafLabel);
                    if (searchOption != null) {
                        searchFieldCombo.setValue(searchOption);
                        searchTextField.requestFocus();
                    }
                    loadBooks();
                    setSearchWebButtonVisible(false);
                    break;
                case "MOVIES":
                    showPane(moviesPane);
                    moviesPaneController.showCategory(leafLabel);
                    break;
                case "MUSIC":
                    showPane(musicPane);
                    musicPaneController.showCategory(leafLabel);
                    break;
                case "ART":
                    showPane(artPane);
                    artPaneController.showCategory(leafLabel);
                    break;
                default: {
                    CustomCategoryPane custom = customPanes.get(branch.getValue());
                    if (custom != null) {
                        showPane(custom);
                        custom.showCategory(leafLabel);
                    } else { // MEDIATYPE
                        showPane(null);
                    }
                }
            }
        }

        // Shows one media pane (or just the logo when pane is null) and hides the rest.
        private void showPane(Node pane) {
            for (Node node : centerStack.getChildren()) {
                if (node == logoImageView) {
                    continue;
                }
                node.setVisible(node == pane);
                node.setManaged(node == pane);
            }
            logoImageView.setVisible(pane == null);
        }

        private String mapLeafToSearchOption(String treeLabel) {
            switch (treeLabel) {
                case "GENRE": return "Genre";
                case "AUTHOR": return "Author";
                case "TITLE": return "Title";
                case "ON HAND": return "On Hand";
                case "LOCATION": return "Location";
                case "HARDBACK": return "Hardback";
                case "PAPERBACK": return "Paperback";
                default: return null; // BOOKS, CONDITION (not searchable)
            }
        }

        private String mapSearchOptionToColumn(String searchOption) {
            switch (searchOption) {
                case "Title": return "title";
                case "Author": return "author";
                case "Genre": return "genre";
                case "On Hand": return "on_hand";
                case "Location": return "location";
                case "Hardback": return "hardback";
                case "Paperback": return "paperback";
                default: return "title";
            }
        }

        private boolean booksTableReady = false;

        // True if the books table already exists. Never creates it.
        private boolean booksTableExists() {
            if (booksTableReady) {
                return true;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables " +
                             "WHERE table_schema = DATABASE() AND table_name = 'books'")) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        booksTableReady = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return booksTableReady;
        }

        // Creates the books table the first time a book is added.
        private void ensureBooksTable() {
            if (booksTableExists()) {
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "title VARCHAR(100) NOT NULL, " +
                            "author VARCHAR(100), " +
                            "genre VARCHAR(100), " +
                            "on_hand INT, " +
                            "hardback BOOLEAN, " +
                            "paperback BOOLEAN, " +
                            "location VARCHAR(50), " +
                            "current_condition VARCHAR(50), " +
                            "number_of_pages INT, " +
                            "selection_value DECIMAL(10,2), " +
                            "INDEX idx_title (title), " +
                            "INDEX idx_author (author), " +
                            "INDEX idx_genre (genre), " +
                            "INDEX idx_on_hand (on_hand), " +
                            "INDEX idx_location (location), " +
                            "INDEX idx_hardback (hardback), " +
                            "INDEX idx_paperback (paperback), " +
                            "INDEX idx_pages (number_of_pages))");
                }
                booksTableReady = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loadBooks() {
            ObservableList<Book> books = FXCollections.observableArrayList();
            if (!booksTableExists()) {
                booksTableView.setItems(books);
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM books");
                fillBooksFromResultSet(rs, books);
            } catch (Exception e) {
                e.printStackTrace();
            }
            booksTableView.setItems(books);
        }

        public void searchBooks() {
            String searchOption = searchFieldCombo.getValue();
            String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();

            if (searchOption == null || term.isEmpty()) {
                loadBooks();
                setSearchWebButtonVisible(false);
                return;
            }

            String column = mapSearchOptionToColumn(searchOption);
            ObservableList<Book> results = FXCollections.observableArrayList();
            if (!booksTableExists()) {
                booksTableView.setItems(results);
                setSearchWebButtonVisible(true);
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();

                if (column.equals("hardback") || column.equals("paperback")) {
                    boolean boolValue = term.equalsIgnoreCase("yes") || term.equalsIgnoreCase("true") || term.equals("1");
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books WHERE " + column + " = ?");
                    stmt.setBoolean(1, boolValue);
                    fillBooksFromResultSet(stmt.executeQuery(), results);
                } else {
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books WHERE " + column + " LIKE ?");
                    stmt.setString(1, "%" + term + "%");
                    fillBooksFromResultSet(stmt.executeQuery(), results);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            booksTableView.setItems(results);
            setSearchWebButtonVisible(results.isEmpty());
        }

        public void showAllBooks() {
            searchTextField.clear();
            loadBooks();
            setSearchWebButtonVisible(false);
        }

        public void showTotalValue() {
            // Only media types you've actually started using (their table exists) are listed.
            List<String> lines = new ArrayList<>();
            double total = 0;

            if (booksTableExists()) {
                double[] books = countAndSum("books");
                lines.add(String.format("Books:  %d items  -  $%,.2f", (int) books[0], books[1]));
                total += books[1];
            }
            if (moviesPaneController.tableExists()) {
                double[] movies = countAndSum("movies");
                lines.add(String.format("Movies: %d items  -  $%,.2f", (int) movies[0], movies[1]));
                total += movies[1];
            }
            if (musicPaneController.tableExists()) {
                double[] music = countAndSum("music");
                lines.add(String.format("Music:  %d items  -  $%,.2f", (int) music[0], music[1]));
                total += music[1];
            }
            if (artPaneController.tableExists()) {
                double[] art = countAndSum("art");
                lines.add(String.format("Art:    %d items  -  $%,.2f", (int) art[0], art[1]));
                total += art[1];
            }
            for (CustomCategoryPane custom : customPanes.values()) {
                double[] values = countAndSum(custom.getCategory().tableName);
                lines.add(String.format("%s: %d items  -  $%,.2f", custom.getCategory().name, (int) values[0], values[1]));
                total += values[1];
            }
            if (lines.isEmpty()) {
                lines.add("Nothing has been added yet.");
            }

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Total Collection Value");
            alert.setHeaderText(null);
            alert.setContentText(String.join("\n", lines) + String.format("%n%nTotal value: $%,.2f", total));
            alert.showAndWait();
        }

        // Returns {number of rows, sum of selection_value} for one media table.
        private double[] countAndSum(String table) {
            double[] result = {0, 0};
            try {
                Connection conn = DatabaseConnection.getConnection();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS item_count, COALESCE(SUM(selection_value), 0) AS total FROM `" + table + "`")) {
                    if (rs.next()) {
                        result[0] = rs.getInt("item_count");
                        result[1] = rs.getDouble("total");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        public void searchWeb() {
            String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
            if (term.isEmpty()) {
                return;
            }
            webView.Main.openWithSearch(term);
        }

        private void setSearchWebButtonVisible(boolean visible) {
            searchWebButton.setVisible(visible);
            searchWebButton.setManaged(visible);
        }

        private void fillBooksFromResultSet(ResultSet rs, ObservableList<Book> books) throws Exception {
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setGenre(rs.getString("genre"));
                book.setOnHand(rs.getInt("on_hand"));
                book.setHardback(rs.getBoolean("hardback"));
                book.setPaperback(rs.getBoolean("paperback"));
                book.setLocation(rs.getString("location"));
                book.setCurrentCondition(rs.getString("current_condition"));
                book.setNumberOfPages(rs.getInt("number_of_pages"));
                book.setSelectionValue(rs.getDouble("selection_value"));
                books.add(book);
            }
        }

        public void addBook() {
            ensureBooksTable();
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "INSERT INTO books (title, author, genre, on_hand, hardback, paperback, location, current_condition, number_of_pages, selection_value) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, titleField.getText());
                stmt.setString(2, authorField.getText());
                stmt.setString(3, genreField.getText());
                stmt.setInt(4, parseIntOrZero(onHandField.getText()));
                stmt.setBoolean(5, hardbackCheck.isSelected());
                stmt.setBoolean(6, paperbackCheck.isSelected());
                stmt.setString(7, locationField.getText());
                stmt.setString(8, conditionField.getText());
                stmt.setInt(9, parseIntOrZero(pagesField.getText()));
                stmt.setDouble(10, parseDoubleOrZero(valueField.getText()));
                stmt.executeUpdate();

                clearForm();
                loadBooks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void deleteBook() {
            Book selected = booksTableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?");
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();

                loadBooks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void clearForm() {
            titleField.clear();
            authorField.clear();
            genreField.clear();
            onHandField.clear();
            hardbackCheck.setSelected(false);
            paperbackCheck.setSelected(false);
            locationField.clear();
            conditionField.clear();
            pagesField.clear();
            valueField.clear();
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
