package libraryCatalog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import libraryCatalog.CustomCategories.FieldType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NewCategoryDialog {

    public static class Result {
        public final String name;
        public final String icon; // image file name
        public final List<String> fieldNames = new ArrayList<>();
        public final List<FieldType> fieldTypes = new ArrayList<>();

        Result(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private static final String TEXT = "Text";
    private static final String NUMBER = "Number";
    private static final String YESNO = "Yes/No";
    private static final int MAX_NAME_LENGTH = 40;

    // existingNamesUpper: names of the custom categories that already exist, in upper case.
    public static Optional<Result> show(Window owner, Set<String> existingNamesUpper) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create your own collection");

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(NewCategoryDialog.class.getResource("Style.css").toExternalForm());
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setPrefWidth(540);

        TextField nameField = new TextField();
        nameField.setPromptText("Category name, e.g. Coins");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        ComboBox<String> iconCombo = new ComboBox<>(FXCollections.observableArrayList(CustomCategories.ICONS.keySet()));
        iconCombo.setValue("Star");
        HBox nameRow = new HBox(8, new Label("Name:"), nameField, new Label("Icon:"), iconCombo);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label standardNote = new Label("Every category also gets: Title, On Hand, Location, Condition and Value.");
        standardNote.setWrapText(true);

        VBox fieldRows = new VBox(6);
        ScrollPane scroll = new ScrollPane(fieldRows);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(220);

        Button addFieldButton = new Button("+ Add Field");
        addFieldButton.setOnAction(e -> addFieldRow(fieldRows, addFieldButton));
        addFieldRow(fieldRows, addFieldButton);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff8a80;");
        errorLabel.setWrapText(true);

        VBox content = new VBox(10, nameRow, standardNote, new Label("Your fields:"), scroll, addFieldButton, errorLabel);
        content.setPadding(new Insets(10, 0, 0, 0));
        pane.setContent(content);

        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, event -> {
            String problem = validate(nameField.getText(), fieldRows, existingNamesUpper);
            if (problem != null) {
                errorLabel.setText(problem);
                event.consume(); // keep the dialog open
            }
        });

        Platform.runLater(nameField::requestFocus);

        Optional<ButtonType> choice = dialog.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return Optional.empty();
        }

        Result result = new Result(nameField.getText().trim(), CustomCategories.ICONS.get(iconCombo.getValue()));
        for (Node row : fieldRows.getChildren()) {
            String name = fieldNameOf(row);
            if (!name.isEmpty()) {
                result.fieldNames.add(name);
                result.fieldTypes.add(fieldTypeOf(row));
            }
        }
        return Optional.of(result);
    }

    private static void addFieldRow(VBox rows, Button addFieldButton) {
        TextField name = new TextField();
        name.setPromptText("Field name, e.g. Year or Grade");
        HBox.setHgrow(name, Priority.ALWAYS);
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList(TEXT, NUMBER, YESNO));
        type.setValue(TEXT);
        Button remove = new Button("X");
        HBox row = new HBox(8, name, type, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        remove.setOnAction(e -> {
            rows.getChildren().remove(row);
            addFieldButton.setDisable(rows.getChildren().size() >= CustomCategories.MAX_FIELDS);
        });
        rows.getChildren().add(row);
        addFieldButton.setDisable(rows.getChildren().size() >= CustomCategories.MAX_FIELDS);
    }

    private static String fieldNameOf(Node row) {
        String text = ((TextField) ((HBox) row).getChildren().get(0)).getText();
        return text == null ? "" : text.trim();
    }

    @SuppressWarnings("unchecked")
    private static FieldType fieldTypeOf(Node row) {
        String type = ((ComboBox<String>) ((HBox) row).getChildren().get(1)).getValue();
        if (NUMBER.equals(type)) {
            return FieldType.NUMBER;
        }
        if (YESNO.equals(type)) {
            return FieldType.YESNO;
        }
        return FieldType.TEXT;
    }

    // Returns a message describing the first problem found, or null if everything is fine.
    private static String validate(String rawName, VBox fieldRows, Set<String> existingNamesUpper) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            return "Please give the category a name.";
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return "The category name can be at most " + MAX_NAME_LENGTH + " characters.";
        }
        String upper = name.toUpperCase();
        if (CustomCategories.RESERVED_CATEGORY_NAMES.contains(upper)) {
            return "\"" + name + "\" is already built in. Please choose a different name.";
        }
        if (existingNamesUpper.contains(upper)) {
            return "You already have a category called \"" + name + "\".";
        }

        Set<String> seen = new HashSet<>();
        for (Node row : fieldRows.getChildren()) {
            String fieldName = fieldNameOf(row);
            if (fieldName.isEmpty()) {
                continue; // blank rows are ignored
            }
            if (fieldName.length() > MAX_NAME_LENGTH) {
                return "Field names can be at most " + MAX_NAME_LENGTH + " characters.";
            }
            String fieldUpper = fieldName.toUpperCase();
            if (CustomCategories.RESERVED_FIELD_NAMES.contains(fieldUpper)) {
                return "\"" + fieldName + "\" is already included in every category. Please choose a different field name.";
            }
            if (!seen.add(fieldUpper)) {
                return "You used the field name \"" + fieldName + "\" twice.";
            }
        }
        return null;
    }
}
