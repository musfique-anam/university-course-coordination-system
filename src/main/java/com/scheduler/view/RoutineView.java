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
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RoutineView {
    private final FileStorage storage;
    private final ObservableList<RoutineEntry> list = FXCollections.observableArrayList();
    private FilteredList<RoutineEntry> filtered;
    private final TableView<RoutineEntry> table = new TableView<>();
    private final VBox root;
    
    // Filter components
    private ComboBox<String> deptFilter;
    private ComboBox<String> batchFilter;
    private ComboBox<String> teacherFilter;
    private ComboBox<String> dayFilter;

    public RoutineView(FileStorage storage) {
        this.storage = storage;
        this.filtered = new FilteredList<>(list, p -> true);
        this.root = build();
        refresh();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        // Title Bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label title = new Label("👁  View & Download Routine");
        title.setStyle(UIStyles.TITLE);
        
        Button downloadBtn = new Button("⬇ Download as TXT");
        downloadBtn.setStyle(UIStyles.BTN_PRIMARY);
        downloadBtn.setOnAction(e -> exportRoutine());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(UIStyles.BTN_BACK);
        refreshBtn.setOnAction(e -> refresh());
        
        Region sp = new Region(); 
        HBox.setHgrow(sp, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, sp, refreshBtn, downloadBtn);

        // Filters Card
        VBox filterCard = new VBox(12);
        filterCard.setPadding(new Insets(16, 20, 16, 20));
        filterCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Create filter comboboxes
        deptFilter = new ComboBox<>();
        deptFilter.setPromptText("All Departments");
        deptFilter.setStyle(UIStyles.COMBO);
        deptFilter.setPrefWidth(150);
        
        batchFilter = new ComboBox<>();
        batchFilter.setPromptText("All Batches");
        batchFilter.setStyle(UIStyles.COMBO);
        batchFilter.setPrefWidth(150);
        
        teacherFilter = new ComboBox<>();
        teacherFilter.setPromptText("All Teachers");
        teacherFilter.setStyle(UIStyles.COMBO);
        teacherFilter.setPrefWidth(150);
        
        dayFilter = new ComboBox<>();
        dayFilter.setPromptText("All Days");
        dayFilter.setStyle(UIStyles.COMBO);
        dayFilter.setPrefWidth(120);

        // Add filter listeners
        deptFilter.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> applyFilters());
        batchFilter.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> applyFilters());
        teacherFilter.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> applyFilters());
        dayFilter.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> applyFilters());

        Button resetBtn = new Button("Reset Filters");
        resetBtn.setStyle(UIStyles.BTN_BACK);
        resetBtn.setOnAction(e -> resetFilters());

        // Use FlowPane instead of HBox to allow wrapping
        FlowPane filterPane = new FlowPane();
        filterPane.setHgap(12);
        filterPane.setVgap(10);
        filterPane.setAlignment(Pos.CENTER_LEFT);
        
        filterPane.getChildren().addAll(
            new Label("Dept:"), deptFilter,
            new Label("Batch:"), batchFilter,
            new Label("Teacher:"), teacherFilter,
            new Label("Day:"), dayFilter,
            resetBtn
        );

        filterCard.getChildren().add(filterPane);

        // Table
        setupTable();

        Label countLbl = new Label();
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #607D8B;");
        
        // Update count when filter changes
        filtered.predicateProperty().addListener((o, oldVal, newVal) -> 
            countLbl.setText("Showing " + filtered.size() + " of " + list.size() + " entries"));

        VBox tableCard = new VBox(12, countLbl, table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, filterCard, tableCard);
        
        return v;
    }

    private void setupTable() {
        TableColumn<RoutineEntry, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDay()));
        dayCol.setPrefWidth(80);

        TableColumn<RoutineEntry, String> slotCol = new TableColumn<>("Time");
        slotCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimeSlot()));
        slotCol.setPrefWidth(100);

        TableColumn<RoutineEntry, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCourseCode() + " - " + (c.getValue().getCourseTitle() != null ? c.getValue().getCourseTitle() : "")));
        courseCol.setPrefWidth(200);

        TableColumn<RoutineEntry, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTeacherName() != null ? c.getValue().getTeacherName() : ""));
        teacherCol.setPrefWidth(150);

        TableColumn<RoutineEntry, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoomNo() != null ? c.getValue().getRoomNo() : "—"));
        roomCol.setPrefWidth(80);

        TableColumn<RoutineEntry, String> deptCol = new TableColumn<>("Dept");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartmentId() != null ? c.getValue().getDepartmentId() : ""));
        deptCol.setPrefWidth(80);

        TableColumn<RoutineEntry, String> batchesCol = new TableColumn<>("Batch(es)");
        batchesCol.setCellValueFactory(c -> new SimpleStringProperty(getBatchNames(c.getValue())));
        batchesCol.setPrefWidth(150);

        TableColumn<RoutineEntry, String> mergedCol = new TableColumn<>("Merged");
        mergedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isMerged() ? "✓" : ""));
        mergedCol.setPrefWidth(60);

        TableColumn<RoutineEntry, Void> editCol = new TableColumn<>("Edit Room");
        editCol.setPrefWidth(100);
        editCol.setCellFactory(cc -> new TableCell<>() {
            private final Button assignRoomBtn = new Button("🚪 Assign");
            {
                assignRoomBtn.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 10;");
                assignRoomBtn.setOnAction(e -> {
                    RoutineEntry entry = getTableRow().getItem();
                    if (entry != null) assignRoom(entry);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(assignRoomBtn);
                }
            }
        });

        table.getColumns().addAll(dayCol, slotCol, courseCol, teacherCol, roomCol, deptCol, batchesCol, mergedCol, editCol);
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(480);
        
        // Double-click to assign room
        table.setRowFactory(tv -> {
            TableRow<RoutineEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    assignRoom(row.getItem());
                }
            });
            return row;
        });
    }

    private String getBatchNames(RoutineEntry entry) {
        List<String> batchNames = new ArrayList<>();
        List<Batch> allBatches = storage.loadBatches();
        
        // Handle multiple batch IDs from batchIds list
        if (entry.getBatchIds() != null && !entry.getBatchIds().isEmpty()) {
            for (String batchId : entry.getBatchIds()) {
                allBatches.stream()
                        .filter(b -> b.getId().equals(batchId))
                        .findFirst()
                        .ifPresent(b -> batchNames.add(b.getName() + " (" + b.getDepartmentId() + ")"));
            }
            return batchNames.isEmpty() ? "—" : String.join(", ", batchNames);
        } 
        // Handle single batch ID
        else if (entry.getBatchId() != null && !entry.getBatchId().isEmpty()) {
            Optional<Batch> batchOpt = allBatches.stream()
                    .filter(b -> b.getId().equals(entry.getBatchId()))
                    .findFirst();
            return batchOpt.map(b -> b.getName() + " (" + b.getDepartmentId() + ")").orElse(entry.getBatchId());
        }
        
        return "—";
    }

    private void assignRoom(RoutineEntry entry) {
        if (entry == null) return;
        
        List<Room> rooms = storage.loadRooms();
        if (rooms.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No rooms available!").showAndWait();
            return;
        }

        ComboBox<Room> roomPicker = new ComboBox<>(FXCollections.observableArrayList(rooms));
        roomPicker.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Room r) { 
                return r != null ? r.getRoomNo() + " (" + r.getRoomType() + ", cap " + r.getCapacity() + ")" : ""; 
            }
            public Room fromString(String s) { return null; }
        });
        roomPicker.setStyle(UIStyles.COMBO);
        roomPicker.setPrefWidth(300);
        
        if (entry.getRoomNo() != null) {
            rooms.stream().filter(r -> r.getRoomNo().equals(entry.getRoomNo())).findFirst().ifPresent(roomPicker::setValue);
        }

        Dialog<String> d = new Dialog<>();
        d.setTitle("Assign Room");
        d.setHeaderText("Assign room to: " + entry.getCourseCode() + " on " + entry.getDay() + " " + entry.getTimeSlot());
        
        VBox content = new VBox(12, new Label("Select Room:"), roomPicker);
        content.setPadding(new Insets(16));
        
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK && roomPicker.getValue() != null) {
                return roomPicker.getValue().getRoomNo();
            }
            return null;
        });
        
        d.showAndWait().ifPresent(roomNo -> {
            entry.setRoomNo(roomNo);
            storage.saveRoutine(new ArrayList<>(list));
            table.refresh();
        });
    }

    private void applyFilters() {
        String selectedDept = deptFilter.getSelectionModel().getSelectedItem();
        String selectedBatch = batchFilter.getSelectionModel().getSelectedItem();
        String selectedTeacher = teacherFilter.getSelectionModel().getSelectedItem();
        String selectedDay = dayFilter.getSelectionModel().getSelectedItem();

        filtered.setPredicate(entry -> {
            // Department filter
            if (selectedDept != null && !selectedDept.isEmpty() && !selectedDept.equals("All Departments")) {
                if (entry.getDepartmentId() == null || !entry.getDepartmentId().equals(selectedDept)) {
                    return false;
                }
            }

            // Batch filter
            if (selectedBatch != null && !selectedBatch.isEmpty() && !selectedBatch.equals("All Batches")) {
                boolean batchMatch = false;
                if (entry.getBatchIds() != null) {
                    batchMatch = entry.getBatchIds().contains(selectedBatch);
                }
                if (!batchMatch && entry.getBatchId() != null) {
                    batchMatch = entry.getBatchId().equals(selectedBatch);
                }
                if (!batchMatch) return false;
            }

            // Teacher filter
            if (selectedTeacher != null && !selectedTeacher.isEmpty() && !selectedTeacher.equals("All Teachers")) {
                if (entry.getTeacherId() == null || !entry.getTeacherId().equals(selectedTeacher)) {
                    return false;
                }
            }

            // Day filter
            if (selectedDay != null && !selectedDay.isEmpty() && !selectedDay.equals("All Days")) {
                if (!selectedDay.equals(entry.getDay())) {
                    return false;
                }
            }

            return true;
        });
    }

    private void resetFilters() {
        deptFilter.getSelectionModel().clearSelection();
        batchFilter.getSelectionModel().clearSelection();
        teacherFilter.getSelectionModel().clearSelection();
        dayFilter.getSelectionModel().clearSelection();
        
        deptFilter.setValue(null);
        batchFilter.setValue(null);
        teacherFilter.setValue(null);
        dayFilter.setValue(null);
        
        filtered.setPredicate(p -> true);
    }

    private void refresh() {
        list.clear();
        list.addAll(storage.loadRoutine());
        populateFilters();
        
        // Update count label
        Label countLbl = (Label) ((VBox) table.getParent()).getChildren().get(0);
        countLbl.setText("Showing " + filtered.size() + " of " + list.size() + " entries");
    }

    private void populateFilters() {
        // Clear existing items
        deptFilter.getItems().clear();
        batchFilter.getItems().clear();
        teacherFilter.getItems().clear();
        dayFilter.getItems().clear();

        // Add "All" options
        deptFilter.getItems().add("All Departments");
        batchFilter.getItems().add("All Batches");
        teacherFilter.getItems().add("All Teachers");
        dayFilter.getItems().add("All Days");

        // Get unique departments from routine entries
        Set<String> depts = list.stream()
                .map(RoutineEntry::getDepartmentId)
                .filter(Objects::nonNull)
                .filter(d -> !d.isEmpty())
                .collect(Collectors.toSet());
        deptFilter.getItems().addAll(depts);

        // Get unique batches from routine entries
        Set<String> batches = new HashSet<>();
        for (RoutineEntry entry : list) {
            if (entry.getBatchIds() != null) {
                batches.addAll(entry.getBatchIds());
            } else if (entry.getBatchId() != null && !entry.getBatchId().isEmpty()) {
                batches.add(entry.getBatchId());
            }
        }
        
        // Get batch names for display
        List<Batch> allBatches = storage.loadBatches();
        List<String> batchDisplayItems = new ArrayList<>();
        for (String batchId : batches) {
            allBatches.stream()
                    .filter(b -> b.getId().equals(batchId))
                    .findFirst()
                    .ifPresent(b -> batchDisplayItems.add(b.getId()));
        }
        batchFilter.getItems().addAll(batchDisplayItems);

        // Get unique teachers from routine entries
        Set<String> teachers = list.stream()
                .map(RoutineEntry::getTeacherId)
                .filter(Objects::nonNull)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
        
        // Get teacher names for display
        List<Teacher> allTeachers = storage.loadTeachers();
        List<String> teacherDisplayItems = new ArrayList<>();
        for (String teacherId : teachers) {
            allTeachers.stream()
                    .filter(t -> t.getTeacherId().equals(teacherId))
                    .findFirst()
                    .ifPresent(t -> teacherDisplayItems.add(teacherId));
        }
        teacherFilter.getItems().addAll(teacherDisplayItems);

        // Add days
        List<String> days = Arrays.asList("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        dayFilter.getItems().addAll(days);

        // Set prompt text
        deptFilter.setPromptText("All Departments");
        batchFilter.setPromptText("All Batches");
        teacherFilter.setPromptText("All Teachers");
        dayFilter.setPromptText("All Days");
    }

    private void exportRoutine() {
        if (filtered.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No routine entries to export!").showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fc.setInitialFileName("routine_" + java.time.LocalDate.now() + ".txt");
        
        File f = fc.showSaveDialog(table.getScene() != null ? table.getScene().getWindow() : null);
        if (f == null) return;
        
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(f.toPath()))) {
            w.println("=" .repeat(80));
            w.println("SMART ACADEMIC SCHEDULER — CLASS ROUTINE");
            w.println("Generated on: " + java.time.LocalDateTime.now());
            w.println("=" .repeat(80));
            w.println();
            
            // Group by day for better formatting
            Map<String, List<RoutineEntry>> byDay = filtered.stream()
                    .collect(Collectors.groupingBy(RoutineEntry::getDay));
            
            for (String day : Arrays.asList("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")) {
                List<RoutineEntry> dayEntries = byDay.get(day);
                if (dayEntries != null && !dayEntries.isEmpty()) {
                    w.println("\n" + day.toUpperCase());
                    w.println("-".repeat(95));
                    w.printf("%-15s %-30s %-20s %-10s %-15s%n", "Time", "Course", "Teacher", "Room", "Batch(es)");
                    w.println("-".repeat(95));
                    
                    for (RoutineEntry e : dayEntries) {
                        w.printf("%-15s %-30s %-20s %-10s %-15s%n",
                                e.getTimeSlot(),
                                (e.getCourseCode() != null ? e.getCourseCode() : "") + " " + (e.getCourseTitle() != null ? e.getCourseTitle() : ""),
                                e.getTeacherName() != null ? e.getTeacherName() : "",
                                e.getRoomNo() != null ? e.getRoomNo() : "—",
                                getBatchNames(e));
                    }
                    w.println();
                }
            }
            
            w.println("\n" + "=".repeat(80));
            w.println("Total Entries: " + filtered.size());
            w.println("Generated by Smart Academic Scheduler v2.0");
            
            new Alert(Alert.AlertType.INFORMATION, "✅ Routine exported successfully to:\n" + f.getPath()).showAndWait();
            
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "❌ Export failed: " + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    public VBox getRoot() { 
        return root; 
    }
}