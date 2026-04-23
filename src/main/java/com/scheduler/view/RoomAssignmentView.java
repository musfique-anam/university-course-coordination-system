package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.FileStorage;
import com.scheduler.util.UIStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class RoomAssignmentView {
    private final FileStorage storage;
    private final ObservableList<Room> list = FXCollections.observableArrayList();
    private FilteredList<Room> filtered;
    private final TableView<Room> table = new TableView<>();
    private final VBox root;

    public RoomAssignmentView(FileStorage storage) {
        this.storage = storage;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label title = new Label("🏗  Room Management — Department Assignment");
        title.setStyle(UIStyles.TITLE);
        titleBar.getChildren().add(title);

        // Summary stats by type
        HBox statsRow = new HBox(16);
        Label totalLbl = mkBadge("Total: 0", "#1565C0");
        Label theoryLbl = mkBadge("Theory: 0", "#2E7D32");
        Label labLbl = mkBadge("Lab: 0", "#AD1457");
        Label assignedLbl = mkBadge("Assigned: 0", "#E65100");
        Label unassignedLbl = mkBadge("Unassigned: 0", "#607D8B");
        statsRow.getChildren().addAll(totalLbl, theoryLbl, labLbl, assignedLbl, unassignedLbl);

        // Filter
        HBox filterBar = new HBox(16);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14, 20, 14, 20));
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All Departments");
        storage.loadDepartments().forEach(d -> deptFilter.getItems().add(d.getId() + " — " + d.getName()));
        deptFilter.setValue("All Departments");
        deptFilter.setStyle(UIStyles.COMBO);
        deptFilter.setPrefWidth(250);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "THEORY", "LAB");
        typeFilter.setValue("All Types");
        typeFilter.setStyle(UIStyles.COMBO);

        CheckBox unassignedOnly = new CheckBox("Show unassigned only");
        unassignedOnly.setStyle("-fx-font-size: 13px;");

        filterBar.getChildren().addAll(new Label("Department:"), deptFilter, new Label("Type:"), typeFilter, unassignedOnly);

        // Bulk assign section
        VBox assignCard = new VBox(12);
        assignCard.setPadding(new Insets(16, 20, 16, 20));
        assignCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label assignTitle = new Label("🏷  Assign Department to Selected Rooms");
        assignTitle.setStyle(UIStyles.SUBTITLE);

        ComboBox<String> assignDeptCombo = new ComboBox<>();
        assignDeptCombo.getItems().add("— Unassign —");
        storage.loadDepartments().forEach(d -> assignDeptCombo.getItems().add(d.getId() + " — " + d.getName()));
        assignDeptCombo.setValue("— Unassign —");
        assignDeptCombo.setStyle(UIStyles.COMBO);
        assignDeptCombo.setPrefWidth(280);

        Button assignBtn = new Button("✓ Assign to Selected");
        assignBtn.setStyle(UIStyles.BTN_SUCCESS);
        assignBtn.setOnAction(e -> {
            List<Room> selected = table.getSelectionModel().getSelectedItems();
            if (selected.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Select rooms from table first!").showAndWait();
                return;
            }
            String deptSel = assignDeptCombo.getValue();
            String deptId = (deptSel == null || deptSel.equals("— Unassign —")) ? null : deptSel.split(" — ")[0];
            selected.forEach(r -> r.setDepartmentId(deptId));
            storage.saveRooms(list);
            table.refresh();
            updateStats(totalLbl, theoryLbl, labLbl, assignedLbl, unassignedLbl);
            new Alert(Alert.AlertType.INFORMATION, "Assignment updated for " + selected.size() + " rooms!").showAndWait();
        });

        HBox assignRow = new HBox(16, new Label("Department:"), assignDeptCombo, assignBtn);
        assignRow.setAlignment(Pos.CENTER_LEFT);
        assignCard.getChildren().addAll(assignTitle, assignRow);

        // Table
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableColumn<Room, String> noCol = col("Room No", c -> new SimpleStringProperty(c.getValue().getRoomNo()));
        TableColumn<Room, String> typeCol = col("Type", c -> new SimpleStringProperty(c.getValue().getRoomType() != null ? c.getValue().getRoomType().name() : ""));
        TableColumn<Room, String> floorCol = col("Floor", c -> new SimpleStringProperty("Floor " + c.getValue().getFloor()));
        TableColumn<Room, String> capCol = col("Cap.", c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacity())));
        TableColumn<Room, String> deptCol = col("Assigned Dept", c -> new SimpleStringProperty(c.getValue().getDepartmentId() != null ? c.getValue().getDepartmentId() : "—"));
        TableColumn<Room, String> projCol = col("Projector", c -> new SimpleStringProperty(c.getValue().isHasProjector() ? "✓" : ""));
        TableColumn<Room, String> pcsCol = col("PCs", c -> new SimpleStringProperty(c.getValue().getTotalPCs() > 0 ? String.valueOf(c.getValue().getTotalPCs()) : ""));

        // Quick assign per row
        TableColumn<Room, Void> quickCol = new TableColumn<>("Quick Assign");
        quickCol.setPrefWidth(200);
        quickCol.setCellFactory(cc -> new TableCell<>() {
            private final ComboBox<String> quickDept = new ComboBox<>();
            {
                quickDept.getItems().add("— None —");
                storage.loadDepartments().forEach(d -> quickDept.getItems().add(d.getId()));
                quickDept.setStyle("-fx-font-size: 11px; -fx-min-width: 100px;");
                quickDept.setOnAction(e -> {
                    Room r = getTableRow().getItem();
                    if (r == null) return;
                    String v = quickDept.getValue();
                    r.setDepartmentId(v.equals("— None —") ? null : v);
                    storage.saveRooms(list);
                    updateStats(totalLbl, theoryLbl, labLbl, assignedLbl, unassignedLbl);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                Room r = getTableRow().getItem();
                quickDept.setValue(r.getDepartmentId() != null ? r.getDepartmentId() : "— None —");
                setGraphic(quickDept);
            }
        });

        table.getColumns().addAll(noCol, typeCol, floorCol, capCol, deptCol, projCol, pcsCol, quickCol);
        filtered = new FilteredList<>(list, r -> true);
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(420);

        deptFilter.setOnAction(e -> applyFilter(deptFilter.getValue(), typeFilter.getValue(), unassignedOnly.isSelected()));
        typeFilter.setOnAction(e -> applyFilter(deptFilter.getValue(), typeFilter.getValue(), unassignedOnly.isSelected()));
        unassignedOnly.setOnAction(e -> applyFilter(deptFilter.getValue(), typeFilter.getValue(), unassignedOnly.isSelected()));

        VBox tableCard = new VBox(12, statsRow, table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, filterBar, assignCard, tableCard);
        refresh();
        updateStats(totalLbl, theoryLbl, labLbl, assignedLbl, unassignedLbl);
        return v;
    }

    private Label mkBadge(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; -fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");
        return l;
    }

    private void updateStats(Label total, Label theory, Label lab, Label assigned, Label unassigned) {
        total.setText("📊 Total: " + list.size());
        long t = list.stream().filter(r -> r.getRoomType() == RoomType.THEORY).count();
        long lb = list.stream().filter(r -> r.getRoomType() == RoomType.LAB).count();
        long a = list.stream().filter(r -> r.getDepartmentId() != null).count();
        long u = list.size() - a;
        theory.setText("📖 Theory: " + t);
        lab.setText("🖥 Lab: " + lb);
        assigned.setText("✅ Assigned: " + a);
        unassigned.setText("⬜ Unassigned: " + u);
    }

    private TableColumn<Room, String> col(String name, javafx.util.Callback<TableColumn.CellDataFeatures<Room, String>, javafx.beans.value.ObservableValue<String>> f) {
        TableColumn<Room, String> c = new TableColumn<>(name);
        c.setCellValueFactory(f);
        return c;
    }

    private void applyFilter(String dept, String type, boolean unassignedOnly) {
        filtered.setPredicate(r -> {
            boolean deptOk = dept == null || dept.equals("All Departments") || (r.getDepartmentId() != null && r.getDepartmentId().equals(dept.split(" — ")[0]));
            boolean typeOk = type == null || type.equals("All Types") || (r.getRoomType() != null && r.getRoomType().name().equals(type));
            boolean unOk = !unassignedOnly || r.getDepartmentId() == null;
            return deptOk && typeOk && unOk;
        });
    }

    private void refresh() {
        list.clear();
        list.addAll(storage.loadRooms());
    }

    public VBox getRoot() { return root; }
}