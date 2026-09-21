package libraryCatalog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import libraryCatalog.CustomCategories.Category;
import libraryCatalog.CustomCategories.Field;
import libraryCatalog.CustomCategories.FieldType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// The pane for one user-made category. Same layout as the built-in Movies/Music/Art panes,
// but assembled in code from the category's field definitions.
public class CustomCategoryPane extends VBox {

    private static class Item {
        final int id;
        final Map<String, String> shown = new HashMap<>(); // column name -> text to display

        Item(int id) {
            this.id = id;
        }
    }

    private static class SearchOption {
        final String column;
        final FieldType type;

        SearchOption(String column, FieldType type) {
            this.column = column;
            this.type = type;
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private final Category category;
    private final Map<String, SearchOption> searchOptions = new LinkedHashMap<>();
    private final Map<Field, Control> fieldInputs = new LinkedHashMap<>();

    private final ComboBox<String> searchFieldCombo = new ComboBox<>();
    private final TextField searchTextField = new TextField();
    private final Button searchWebButton = new Button("Not found - Search the Web");
    private final TableView<Item> table = new TableView<>();

    private final TextField titleField = new TextField();
    private final TextField onHandField = new TextField();
    private final TextField locationField = new TextField();
    private final TextField conditionField = new TextField();
    private final TextField valueField = new TextField();

    public CustomCategoryPane(Category category) {
        this.category = category;
        setSpacing(8);
        setPadding(new Insets(10));
        setVisible(false);
        setManaged(false);

        searchOptions.put("Title", new SearchOption("title", FieldType.TEXT));
        for (Field f : category.fields) {
            searchOptions.put(f.name, new SearchOption(f.columnName, f.type));
        }
        searchOptions.put("On Hand", new SearchOption("on_hand", FieldType.NUMBER));
        searchOptions.put("Location", new SearchOption("location", FieldType.TEXT));

        searchFieldCombo.setItems(FXCollections.observableArrayList(searchOptions.keySet()));
        searchFieldCombo.setValue("Title");
        searchFieldCombo.setPrefWidth(150);
        searchTextField.setPromptText("Search term");
        searchTextField.setPrefWidth(200);
        searchTextField.setOnAction(e -> search());
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> search());
        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(e -> showAll());
        searchWebButton.setOnAction(e -> searchWeb());
        searchWebButton.setVisible(false);
        searchWebButton.setManaged(false);
        HBox searchBar = new HBox(8, new Label("Search by:"), searchFieldCombo, searchTextField,
                searchButton, showAllButton, searchWebButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        addColumn("Title", "title", 150, false);
        for (Field f : category.fields) {
            addColumn(f.name, f.columnName, 100, f.type == FieldType.YESNO);
        }
        addColumn("On Hand", "on_hand", 60, false);
        addColumn("Location", "location", 90, false);
        addColumn("Condition", "current_condition", 80, false);
        addColumn("Value", "selection_value", 60, false);
        VBox.setVgrow(table, Priority.ALWAYS);

        FlowPane form = new FlowPane(8, 8);
        form.setRowValignment(VPos.CENTER);
        titleField.setPromptText("Title");
        titleField.setPrefWidth(180);
        form.getChildren().add(titleField);
        for (Field f : category.fields) {
            Control input;
            if (f.type == FieldType.YESNO) {
                input = new CheckBox(f.name);
            } else {
                TextField textField = new TextField();
                textField.setPromptText(f.type == FieldType.NUMBER ? f.name + " (number)" : f.name);
                textField.setPrefWidth(150);
                input = textField;
            }
            fieldInputs.put(f, input);
            form.getChildren().add(input);
        }
        onHandField.setPromptText("On Hand");
        onHandField.setPrefWidth(70);
        locationField.setPromptText("Location");
        conditionField.setPromptText("Condition");
        valueField.setPromptText("Value");
        valueField.setPrefWidth(70);
        form.getChildren().addAll(onHandField, locationField, conditionField, valueField);

        Button addButton = new Button("Add Item");
        addButton.setOnAction(e -> add());
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelected());
        HBox buttons = new HBox(8, addButton, deleteButton);

        getChildren().addAll(searchBar, table, form, buttons);
    }

    public Category getCategory() {
        return category;
    }

    // Called by the main controller each time one of this category's tree items is selected.
    public void showCategory(String treeLabel) {
        if (treeLabel != null) {
            for (String label : searchOptions.keySet()) {
                if (label.toUpperCase().equals(treeLabel)) {
                    searchFieldCombo.setValue(label);
                    searchTextField.requestFocus();
                    break;
                }
            }
        }
        table.setItems(query("", null));
        setSearchWebButtonVisible(false);
    }

    public int countRows() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + category.tableName + "`")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void addColumn(String title, String key, double width, boolean centered) {
        TableColumn<Item, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().shown.getOrDefault(key, "")));
        if (centered) {
            column.setStyle("-fx-alignment: CENTER;");
        }
        table.getColumns().add(column);
    }

    private void setSearchWebButtonVisible(boolean visible) {
        searchWebButton.setVisible(visible);
        searchWebButton.setManaged(visible);
    }

    // Runs SELECT * on this category's table with an optional WHERE clause built from generated column names.
    private ObservableList<Item> query(String where, Binder binder) {
        ObservableList<Item> items = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM `" + category.tableName + "`" + where)) {
                if (binder != null) {
                    binder.bind(ps);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(readItem(rs));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private Item readItem(ResultSet rs) throws SQLException {
        Item item = new Item(rs.getInt("id"));
        item.shown.put("title", orEmpty(rs.getString("title")));
        for (Field f : category.fields) {
            if (f.type == FieldType.YESNO) {
                item.shown.put(f.columnName, rs.getBoolean(f.columnName) ? "✓" : "");
            } else if (f.type == FieldType.NUMBER) {
                BigDecimal number = rs.getBigDecimal(f.columnName);
                item.shown.put(f.columnName, number == null ? "" : number.stripTrailingZeros().toPlainString());
            } else {
                item.shown.put(f.columnName, orEmpty(rs.getString(f.columnName)));
            }
        }
        item.shown.put("on_hand", String.valueOf(rs.getInt("on_hand")));
        item.shown.put("location", orEmpty(rs.getString("location")));
        item.shown.put("current_condition", orEmpty(rs.getString("current_condition")));
        BigDecimal value = rs.getBigDecimal("selection_value");
        item.shown.put("selection_value", value == null ? "" : value.toPlainString());
        return item;
    }

    private static String orEmpty(String text) {
        return text == null ? "" : text;
    }

    public void search() {
        String label = searchFieldCombo.getValue();
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
        if (label == null || term.isEmpty()) {
            showAll();
            return;
        }

        SearchOption option = searchOptions.get(label);
        String column = option.column;
        ObservableList<Item> results;

        if (option.type == FieldType.YESNO) {
            boolean yes = term.equalsIgnoreCase("yes") || term.equalsIgnoreCase("true") || term.equals("1");
            results = query(" WHERE `" + column + "` = ?", ps -> ps.setBoolean(1, yes));
        } else if (option.type == FieldType.NUMBER) {
            try {
                BigDecimal number = new BigDecimal(term);
                results = query(" WHERE `" + column + "` = ?", ps -> ps.setBigDecimal(1, number));
            } catch (NumberFormatException e) {
                results = FXCollections.observableArrayList(); // not a number, so nothing can match
            }
        } else {
            results = query(" WHERE `" + column + "` LIKE ?", ps -> ps.setString(1, "%" + term + "%"));
        }

        table.setItems(results);
        setSearchWebButtonVisible(results.isEmpty());
    }

    public void showAll() {
        searchTextField.clear();
        table.setItems(query("", null));
        setSearchWebButtonVisible(false);
    }

    private void searchWeb() {
        String term = searchTextField.getText() == null ? "" : searchTextField.getText().trim();
        if (term.isEmpty()) {
            return;
        }
        if ("Title".equals(searchFieldCombo.getValue())) {
            term = term + " " + category.name;
        }
        webView.Main.openWithSearch(term);
    }

    private void add() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            return;
        }

        StringBuilder columns = new StringBuilder("title");
        StringBuilder marks = new StringBuilder("?");
        for (Field f : category.fields) {
            columns.append(", `").append(f.columnName).append("`");
            marks.append(", ?");
        }
        columns.append(", on_hand, location, current_condition, selection_value");
        marks.append(", ?, ?, ?, ?");
        String sql = "INSERT INTO `" + category.tableName + "` (" + columns + ") VALUES (" + marks + ")";

        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                ps.setString(i++, title);
                for (Field f : category.fields) {
                    Control input = fieldInputs.get(f);
                    if (f.type == FieldType.YESNO) {
                        ps.setBoolean(i++, ((CheckBox) input).isSelected());
                        continue;
                    }
                    String text = ((TextField) input).getText() == null ? "" : ((TextField) input).getText().trim();
                    if (f.type == FieldType.NUMBER) {
                        try {
                            ps.setBigDecimal(i, new BigDecimal(text));
                        } catch (NumberFormatException e) {
                            ps.setNull(i, Types.DECIMAL);
                        }
                    } else if (text.isEmpty()) {
                        ps.setNull(i, Types.VARCHAR);
                    } else {
                        ps.setString(i, text);
                    }
                    i++;
                }
                ps.setInt(i++, parseIntOrZero(onHandField.getText()));
                ps.setString(i++, locationField.getText());
                ps.setString(i++, conditionField.getText());
                ps.setDouble(i, parseDoubleOrZero(valueField.getText()));
                ps.executeUpdate();
            }
            clearForm();
            showAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelected() {
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM `" + category.tableName + "` WHERE id = ?")) {
                ps.setInt(1, selected.id);
                ps.executeUpdate();
            }
            showAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.clear();
        for (Control input : fieldInputs.values()) {
            if (input instanceof CheckBox) {
                ((CheckBox) input).setSelected(false);
            } else {
                ((TextField) input).clear();
            }
        }
        onHandField.clear();
        locationField.clear();
        conditionField.clear();
        valueField.clear();
    }

    private static int parseIntOrZero(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
