package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.util.UIStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RoomManageView {
    private final DatabaseStorage storage;
    private final ObservableList<Room> list = FXCollections.observableArrayList();
    private FilteredList<Room> filtered;
    private final TableView<Room> table = new TableView<>();
    private final VBox root;

    public RoomManageView(DatabaseStorage storage) {
        this.storage = storage;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        // Title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label title = new Label("🚪  Room Management");
        title.setStyle(UIStyles.TITLE);
        Button addBtn = new Button("+ Add Room");
        addBtn.setStyle(UIStyles.BTN_SUCCESS);
        addBtn.setOnAction(e -> showAddRoomDialog());
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, sp, addBtn);

        // Filter bar
        HBox filterBar = new HBox(16);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(14, 20, 14, 20));
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "THEORY", "LAB");
        typeFilter.setValue("All Types");
        typeFilter.setStyle(UIStyles.COMBO);

        ComboBox<String> floorFilter = new ComboBox<>();
        floorFilter.getItems().add("All Floors");
        for (int f = 1; f <= 20; f++) floorFilter.getItems().add("Floor " + f);
        floorFilter.setValue("All Floors");
        floorFilter.setStyle(UIStyles.COMBO);

        filterBar.getChildren().addAll(new Label("Type:"), typeFilter, new Label("Floor:"), floorFilter);

        // Stats row
        HBox statsRow = new HBox(16);
        Label totalLbl = new Label();
        Label theoryLbl = new Label();
        Label labLbl = new Label();
        for (Label l : new Label[]{totalLbl, theoryLbl, labLbl}) {
            l.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
        statsRow.getChildren().addAll(totalLbl, theoryLbl, labLbl);

        // Table
        TableColumn<Room, String> noCol = col("Room No", c -> new SimpleStringProperty(c.getValue().getRoomNo()));
        TableColumn<Room, String> typeCol = col("Type", c -> new SimpleStringProperty(c.getValue().getRoomType() != null ? c.getValue().getRoomType().name() : ""));
        TableColumn<Room, String> floorCol = col("Floor", c -> new SimpleStringProperty(String.valueOf(c.getValue().getFloor())));
        TableColumn<Room, String> capCol = col("Capacity", c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacity())));
        TableColumn<Room, String> projCol = col("Projector", c -> new SimpleStringProperty(c.getValue().isHasProjector() ? "✓" : ""));
        TableColumn<Room, String> soundCol = col("Sound", c -> new SimpleStringProperty(c.getValue().isHasSoundSystem() ? "✓" : ""));
        TableColumn<Room, String> pcsCol = col("PCs", c -> new SimpleStringProperty(c.getValue().getTotalPCs() > 0 ? String.valueOf(c.getValue().getTotalPCs()) : ""));
        TableColumn<Room, String> deptCol = col("Assigned Dept", c -> new SimpleStringProperty(c.getValue().getDepartmentId() != null ? c.getValue().getDepartmentId() : "—"));

        TableColumn<Room, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(190);
        actCol.setCellFactory(cc -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑");
            {
                editBtn.setStyle(UIStyles.BTN_WARNING + "-fx-font-size: 11px; -fx-padding: 5 10;");
                delBtn.setStyle(UIStyles.BTN_DANGER + "-fx-font-size: 11px; -fx-padding: 5 8;");
                editBtn.setOnAction(e -> editRoom(getTableRow().getItem()));
                delBtn.setOnAction(e -> deleteRoom(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(noCol, typeCol, floorCol, capCol, projCol, soundCol, pcsCol, deptCol, actCol);
        filtered = new FilteredList<>(list, r -> true);
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(480);

        typeFilter.setOnAction(e -> applyFilter(typeFilter.getValue(), floorFilter.getValue()));
        floorFilter.setOnAction(e -> applyFilter(typeFilter.getValue(), floorFilter.getValue()));

        VBox tableCard = new VBox(12, statsRow, table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, filterBar, tableCard);

        refresh();
        updateStats(totalLbl, theoryLbl, labLbl);
        list.addListener((javafx.collections.ListChangeListener<Room>) c -> updateStats(totalLbl, theoryLbl, labLbl));

        return v;
    }

    private void updateStats(Label total, Label theory, Label lab) {
        total.setText("📊 Total: " + list.size());
        long t = list.stream().filter(r -> r.getRoomType() == RoomType.THEORY).count();
        long l = list.stream().filter(r -> r.getRoomType() == RoomType.LAB).count();
        theory.setText("📖 Theory: " + t);
        lab.setText("🖥 Lab: " + l);
    }

    private TableColumn<Room, String> col(String name, javafx.util.Callback<TableColumn.CellDataFeatures<Room, String>, javafx.beans.value.ObservableValue<String>> f) {
        TableColumn<Room, String> c = new TableColumn<>(name);
        c.setCellValueFactory(f);
        return c;
    }

    private void applyFilter(String type, String floor) {
        filtered.setPredicate(r -> {
            boolean typeOk = type == null || type.equals("All Types") || (r.getRoomType() != null && r.getRoomType().name().equals(type));
            boolean floorOk = floor == null || floor.equals("All Floors") || ("Floor " + r.getFloor()).equals(floor);
            return typeOk && floorOk;
        });
    }

    private void editRoom(Room r) {
        if (r == null) return;
        Dialog<Boolean> d = new Dialog<>();
        d.setTitle("Edit Room: " + r.getRoomNo());
        d.getDialogPane().setPrefWidth(420);

        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));
        typeCombo.setStyle(UIStyles.COMBO);
        typeCombo.setValue(r.getRoomType() != null ? r.getRoomType().name() : "THEORY");

        Spinner<Integer> capSpinner = new Spinner<>(1, 500, r.getCapacity());
        capSpinner.setEditable(true);
        CheckBox projector = new CheckBox("Projector");
        projector.setSelected(r.isHasProjector());
        CheckBox sound = new CheckBox("Sound System");
        sound.setSelected(r.isHasSoundSystem());
        Spinner<Integer> totalPCs = new Spinner<>(0, 200, r.getTotalPCs());
        totalPCs.setEditable(true);
        Spinner<Integer> activePCs = new Spinner<>(0, 200, r.getActivePCs());
        activePCs.setEditable(true);

        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().add("— None —");
        storage.loadDepartments().forEach(dep -> deptCombo.getItems().add(dep.getId() + " — " + dep.getName()));
        if (r.getDepartmentId() != null) {
            deptCombo.getItems().stream().filter(s -> s.startsWith(r.getDepartmentId())).findFirst().ifPresent(deptCombo::setValue);
        } else {
            deptCombo.setValue("— None —");
        }
        deptCombo.setStyle(UIStyles.COMBO);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(16));
        form.add(new Label("Room Type:"), 0, 0); form.add(typeCombo, 1, 0);
        form.add(new Label("Capacity:"), 0, 1); form.add(capSpinner, 1, 1);
        form.add(projector, 0, 2, 2, 1);
        form.add(sound, 0, 3, 2, 1);
        form.add(new Label("Total PCs:"), 0, 4); form.add(totalPCs, 1, 4);
        form.add(new Label("Active PCs:"), 0, 5); form.add(activePCs, 1, 5);
        form.add(new Label("Assigned Dept:"), 0, 6); form.add(deptCombo, 1, 6);

        d.getDialogPane().setContent(form);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> btn == ButtonType.OK);
        d.showAndWait().ifPresent(ok -> {
            if (ok) {
                r.setRoomType("LAB".equals(typeCombo.getValue()) ? RoomType.LAB : RoomType.THEORY);
                r.setCapacity(capSpinner.getValue());
                r.setHasProjector(projector.isSelected());
                r.setHasSoundSystem(sound.isSelected());
                r.setTotalPCs(totalPCs.getValue());
                r.setActivePCs(activePCs.getValue());
                String deptSel = deptCombo.getValue();
                r.setDepartmentId(deptSel != null && !deptSel.equals("— None —") ? deptSel.split(" — ")[0] : null);
                storage.saveRooms(list);
                table.refresh();
            }
        });
    }

    private void deleteRoom(Room r) {
        if (r == null) return;
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete room: " + r.getRoomNo() + "?");
        c.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) { list.remove(r); storage.saveRooms(list); } });
    }

    private void showAddRoomDialog() {
        Dialog<Room> d = new Dialog<>();
        d.setTitle("Add New Room");
        d.getDialogPane().setPrefSize(500, 500);

        TextField noField = new TextField();
        noField.setPromptText("e.g. NB-101, NB-507...");
        noField.setStyle(UIStyles.INPUT);
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));
        typeCombo.setStyle(UIStyles.COMBO);
        typeCombo.getSelectionModel().select(0);
        Spinner<Integer> floor = new Spinner<>(1, 20, 1);
        floor.setEditable(true);
        Spinner<Integer> capSpinner = new Spinner<>(1, 500, 50);
        capSpinner.setEditable(true);
        CheckBox projector = new CheckBox("Has Projector");
        CheckBox sound = new CheckBox("Has Sound System");
        Spinner<Integer> totalPCs = new Spinner<>(0, 200, 0);
        totalPCs.setEditable(true);
        Spinner<Integer> activePCs = new Spinner<>(0, 200, 0);
        activePCs.setEditable(true);
        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().add("— None —");
        storage.loadDepartments().forEach(dep -> deptCombo.getItems().add(dep.getId() + " — " + dep.getName()));
        deptCombo.setValue("— None —");
        deptCombo.setStyle(UIStyles.COMBO);

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(12); form.setPadding(new Insets(16));
        form.add(new Label("Room No *:"), 0, 0); form.add(noField, 1, 0);
        form.add(new Label("Room Type:"), 0, 1); form.add(typeCombo, 1, 1);
        form.add(new Label("Floor:"), 0, 2); form.add(floor, 1, 2);
        form.add(new Label("Capacity:"), 0, 3); form.add(capSpinner, 1, 3);
        form.add(projector, 0, 4, 2, 1);
        form.add(sound, 0, 5, 2, 1);
        form.add(new Label("Total PCs (Lab):"), 0, 6); form.add(totalPCs, 1, 6);
        form.add(new Label("Active PCs (Lab):"), 0, 7); form.add(activePCs, 1, 7);
        form.add(new Label("Assign to Dept:"), 0, 8); form.add(deptCombo, 1, 8);

        d.getDialogPane().setContent(form);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String no = noField.getText() != null ? noField.getText().trim() : "";
            if (no.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Room No required!").showAndWait(); return null; }
            if (list.stream().anyMatch(r -> no.equals(r.getRoomNo()))) {
                new Alert(Alert.AlertType.WARNING, "Room already exists!").showAndWait(); return null;
            }
            Room r = new Room();
            r.setRoomNo(no);
            r.setRoomType("LAB".equals(typeCombo.getValue()) ? RoomType.LAB : RoomType.THEORY);
            r.setFloor(floor.getValue());
            r.setCapacity(capSpinner.getValue());
            r.setHasProjector(projector.isSelected());
            r.setHasSoundSystem(sound.isSelected());
            r.setTotalPCs(totalPCs.getValue());
            r.setActivePCs(activePCs.getValue());
            String deptSel = deptCombo.getValue();
            r.setDepartmentId(deptSel != null && !deptSel.equals("— None —") ? deptSel.split(" — ")[0] : null);
            return r;
        });
        d.showAndWait().ifPresent(r -> { if (r != null) { list.add(r); storage.saveRooms(list); } });
    }

    private void refresh() {
        list.clear();
        list.addAll(storage.loadRooms());
    }

    public VBox getRoot() { return root; }
}