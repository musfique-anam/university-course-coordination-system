package com.scheduler.view;

import com.scheduler.model.Department;
import com.scheduler.storage.FileStorage;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Optional;

public class DepartmentManageView {

    private final FileStorage storage;
    private final ObservableList<Department> list = FXCollections.observableArrayList();
    private final TableView<Department> table = new TableView<>();
    private final VBox root;
    private final Label statsBadge = new Label();

    public DepartmentManageView(FileStorage storage) {
        this.storage = storage;
        this.root = build();
        loadInitialData();
    }

    private VBox build() {

        VBox main = new VBox();
        main.setStyle("-fx-background-color: #F1F5F9;");
        main.setSpacing(0);

        /* ================= HERO ================= */
        StackPane hero = new StackPane();
        hero.setPrefHeight(180);
        hero.setStyle("-fx-background-color: linear-gradient(to right, #1E3A8A, #2563EB);");

        for (int i = 0; i < 3; i++) {
            Circle c = new Circle(40 + i * 15);
            c.setFill(Color.web("#FFFFFF", 0.06));
            c.setTranslateX(-250 + i * 200);
            hero.getChildren().add(c);
        }

        VBox heroContent = new VBox(5);
        heroContent.setAlignment(Pos.CENTER);

        Label title = new Label("Department Management");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        statsBadge.setStyle("-fx-font-size: 14px; -fx-text-fill: #E0F2FE;");

        heroContent.getChildren().addAll(title, statsBadge);
        hero.getChildren().add(heroContent);

        /* ================= MAIN CARD ================= */
        VBox card = new VBox(25);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(1000);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 18;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 25, 0, 0, 8);
        """);

        card.setTranslateY(-50);

        /* ================= SEARCH ================= */
        TextField searchField = new TextField();
        searchField.setPromptText("Search department...");
        searchField.setMaxWidth(420);
        searchField.setStyle("""
            -fx-background-radius: 25;
            -fx-padding: 10 18;
            -fx-border-radius: 25;
            -fx-border-color: #CBD5E1;
            -fx-font-size: 14px;
        """);

        /* ================= ADD FORM ================= */
        HBox addRow = new HBox(15);
        addRow.setAlignment(Pos.CENTER);

        TextField idInput = new TextField();
        idInput.setPromptText("Code (CSE)");
        idInput.setPrefWidth(140);

        TextField nameInput = new TextField();
        nameInput.setPromptText("Department Name");
        nameInput.setPrefWidth(300);

        Button addBtn = new Button("Add");
        addBtn.setCursor(Cursor.HAND);
        addBtn.setStyle("""
            -fx-background-color: #2563EB;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 25;
            -fx-background-radius: 25;
        """);

        addBtn.setOnAction(e -> {
            String code = idInput.getText().trim().toUpperCase();
            String name = nameInput.getText().trim();

            if (code.isEmpty() || name.isEmpty()) {
                showAlert("Validation Error", "Fields cannot be empty.");
                return;
            }

            for (Department d : list) {
                if (d.getId().equalsIgnoreCase(code)) {
                    showAlert("Duplicate Error", "Department code already exists.");
                    return;
                }
            }

            list.add(new Department(code, name));
            storage.saveDepartments(list);

            idInput.clear();
            nameInput.clear();
            updateStats();
        });

        addRow.getChildren().addAll(idInput, nameInput, addBtn);

        /* ================= TABLE ================= */
        setupTable();

        table.setFixedCellSize(50);
        table.prefHeightProperty().bind(
                table.fixedCellSizeProperty()
                        .multiply(Bindings.size(list).add(1))
                        .add(35)
        );

        table.setMaxWidth(900);

        /* ================= FILTER ================= */
        FilteredList<Department> filteredData = new FilteredList<>(list, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(dept -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return dept.getName().toLowerCase().contains(lower)
                        || dept.getId().toLowerCase().contains(lower);
            });
        });
        table.setItems(filteredData);

        card.getChildren().addAll(searchField, addRow, table);

        /* ================= WRAPPER ================= */
        StackPane centerWrap = new StackPane(card);
        centerWrap.setPadding(new Insets(0, 40, 40, 40));
        centerWrap.setAlignment(Pos.TOP_CENTER);

        main.getChildren().addAll(hero, centerWrap);

        return main;
    }

    private void setupTable() {

        table.setRowFactory(tv -> {
            TableRow<Department> row = new TableRow<>();
            row.setPrefHeight(50);

            row.setOnMouseEntered(e ->
                    row.setStyle("-fx-background-color: #F8FAFC;")
            );
            row.setOnMouseExited(e ->
                    row.setStyle("-fx-background-color: white;")
            );

            return row;
        });

        TableColumn<Department, String> idCol = new TableColumn<>("Code");
        idCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        idCol.setPrefWidth(150);
        idCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<Department, String> nameCol = new TableColumn<>("Department Name");
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Department, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(8, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white;");
                delBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;");
                box.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                delBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadInitialData() {
        list.clear();
        List<Department> saved = storage.loadDepartments();

        if (saved == null || saved.isEmpty()) {
            String[][] depts = {
                    {"CSE", "Computer Science & Engineering"},
                    {"EEE", "Electrical & Electronic Engineering"},
                    {"BBA", "Business Administration"},
                    {"CIVIL", "Civil Engineering"},
                    {"ENG", "English"},
                    {"SOC", "Sociology"}
            };
            for (String[] d : depts) {
                list.add(new Department(d[0], d[1]));
            }
            storage.saveDepartments(list);
        } else {
            list.addAll(saved);
        }
        updateStats();
    }

    /* ================= EDIT WITH CODE + NAME ================= */
    private void handleEdit(Department dept) {
        if (dept == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Department");

        TextField codeField = new TextField(dept.getId());
        TextField nameField = new TextField(dept.getName());

        VBox box = new VBox(10,
                new Label("Department Code:"), codeField,
                new Label("Department Name:"), nameField
        );
        box.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newCode = codeField.getText().trim().toUpperCase();
            String newName = nameField.getText().trim();

            if (newCode.isEmpty() || newName.isEmpty()) {
                showAlert("Validation Error", "Fields cannot be empty.");
                return;
            }

            for (Department d : list) {
                if (d != dept && d.getId().equalsIgnoreCase(newCode)) {
                    showAlert("Duplicate Error", "Department code already exists.");
                    return;
                }
            }

            dept.setId(newCode);
            dept.setName(newName);

            storage.saveDepartments(list);
            table.refresh();
            updateStats();
        }
    }

    /* ================= DELETE WITH CONFIRMATION ================= */
    private void handleDelete(Department dept) {
        if (dept == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Department");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete department: " + dept.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            list.remove(dept);
            storage.saveDepartments(list);
            updateStats();
        }
    }

    private void updateStats() {
        statsBadge.setText(list.size() + " active departments");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}
