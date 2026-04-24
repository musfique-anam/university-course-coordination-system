package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.util.TimeRules;
import com.scheduler.util.UIStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class MergeClassView {
    private final DatabaseStorage storage;
    private final ObservableList<MergedClassOption> list = FXCollections.observableArrayList();
    private final TableView<MergedClassOption> table = new TableView<>();
    private final VBox root;
    private ComboBox<String> deptFilterCombo;
    private ComboBox<String> batchFilterCombo;
    private FilteredList<MergedClassOption> filteredList;

    public MergeClassView(DatabaseStorage storage) {
        this.storage = storage;
        this.root = build();
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
        
        Label title = new Label("🔀  Merged Class Management (3-Batch Merge)");
        title.setStyle(UIStyles.TITLE);
        
        Button addBtn = new Button("+ New 3-Batch Merge");
        addBtn.setStyle(UIStyles.BTN_SUCCESS);
        addBtn.setOnAction(e -> showAddMergeDialog());
        
        Region sp = new Region(); 
        HBox.setHgrow(sp, Priority.ALWAYS);
        
        // Filter controls
        Label filterLabel = new Label("Filter by:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64;");
        
        deptFilterCombo = new ComboBox<>();
        deptFilterCombo.getItems().add("All Departments");
        deptFilterCombo.setValue("All Departments");
        deptFilterCombo.setStyle(UIStyles.COMBO);
        deptFilterCombo.setPrefWidth(180);
        deptFilterCombo.setOnAction(e -> applyFilters());
        
        batchFilterCombo = new ComboBox<>();
        batchFilterCombo.getItems().add("All Batches");
        batchFilterCombo.setValue("All Batches");
        batchFilterCombo.setStyle(UIStyles.COMBO);
        batchFilterCombo.setPrefWidth(180);
        batchFilterCombo.setOnAction(e -> applyFilters());
        
        HBox filterBox = new HBox(10, filterLabel, deptFilterCombo, batchFilterCombo);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        titleBar.getChildren().addAll(title, sp, filterBox, addBtn);

        // Info Label
        Label infoLbl = new Label("ℹ  Create a 3-batch merged class: Select 1 primary batch + 2 additional batches. They can be from same or different departments. All batches share same teacher, room, time slot, and courses.");
        infoLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #607D8B; -fx-wrap-text: true;");
        infoLbl.setWrapText(true);

        // Table Columns
        TableColumn<MergedClassOption, String> batchesCol = new TableColumn<>("Batches (Primary + 2)");
        batchesCol.setCellValueFactory(c -> new SimpleStringProperty(formatBatches(c.getValue())));
        batchesCol.setPrefWidth(300);
        
        TableColumn<MergedClassOption, String> deptCol = new TableColumn<>("Departments");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(formatDepartments(c.getValue())));
        deptCol.setPrefWidth(150);
        
        TableColumn<MergedClassOption, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(c -> new SimpleStringProperty(getTeacherName(c.getValue().getTeacherId())));
        teacherCol.setPrefWidth(120);
        
        TableColumn<MergedClassOption, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNo()));
        roomCol.setPrefWidth(80);
        
        TableColumn<MergedClassOption, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDay()));
        dayCol.setPrefWidth(80);
        
        TableColumn<MergedClassOption, String> slotCol = new TableColumn<>("Time Slot");
        slotCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimeSlot()));
        slotCol.setPrefWidth(100);
        
        TableColumn<MergedClassOption, String> courseCol = new TableColumn<>("Courses");
        courseCol.setCellValueFactory(c -> new SimpleStringProperty(formatCourses(c.getValue())));
        courseCol.setPrefWidth(200);

        TableColumn<MergedClassOption, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(100);
        actCol.setCellFactory(cc -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑 Delete");
            private final Button generateBtn = new Button("📅 Generate");
            
            {
                editBtn.setStyle(UIStyles.BTN_WARNING + "-fx-font-size: 11px; -fx-padding: 5 10;");
                delBtn.setStyle(UIStyles.BTN_DANGER + "-fx-font-size: 11px; -fx-padding: 5 10;");
                generateBtn.setStyle(UIStyles.BTN_PRIMARY + "-fx-font-size: 11px; -fx-padding: 5 10;");
                
                editBtn.setOnAction(e -> showEditMergeDialog(getTableRow().getItem()));
                delBtn.setOnAction(e -> deleteMerge(getTableRow().getItem()));
                generateBtn.setOnAction(e -> generateRoutineForMerge(getTableRow().getItem()));
            }
            
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, generateBtn, delBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(batchesCol, deptCol, teacherCol, roomCol, dayCol, slotCol, courseCol, actCol);
        
        // Setup filtered list
        filteredList = new FilteredList<>(list, p -> true);
        table.setItems(filteredList);
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(400);

        VBox tableCard = new VBox(12, infoLbl, table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, tableCard);
        refresh();
        updateFilters();
        return v;
    }

    private void applyFilters() {
        if (filteredList == null) return;
        
        String selectedDept = deptFilterCombo.getValue();
        String selectedBatch = batchFilterCombo.getValue();
        
        filteredList.setPredicate(option -> {
            // Department filter
            if (selectedDept != null && !selectedDept.equals("All Departments")) {
                String deptId = selectedDept.split(" — ")[0];
                List<Batch> batches = storage.loadBatches();
                boolean hasDept = option.getBatchIds().stream()
                        .map(batchId -> batches.stream().filter(b -> b.getId().equals(batchId)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .anyMatch(b -> deptId.equals(b.getDepartmentId()));
                if (!hasDept) return false;
            }
            
            // Batch filter
            if (selectedBatch != null && !selectedBatch.equals("All Batches")) {
                String batchId = selectedBatch.split(" — ")[0];
                if (!option.getBatchIds().contains(batchId)) return false;
            }
            
            return true;
        });
    }

    private void updateFilters() {
        // Update department filter
        Set<String> depts = new HashSet<>();
        List<Department> allDepts = storage.loadDepartments();
        
        for (MergedClassOption opt : list) {
            for (String batchId : opt.getBatchIds()) {
                storage.loadBatches().stream()
                        .filter(b -> b.getId().equals(batchId))
                        .findFirst()
                        .ifPresent(b -> depts.add(b.getDepartmentId()));
            }
        }
        
        String currentDept = deptFilterCombo.getValue();
        deptFilterCombo.getItems().clear();
        deptFilterCombo.getItems().add("All Departments");
        
        for (String deptId : depts) {
            allDepts.stream()
                    .filter(d -> deptId.equals(d.getId()))
                    .findFirst()
                    .ifPresent(d -> deptFilterCombo.getItems().add(d.getId() + " — " + d.getName()));
        }
        
        if (currentDept != null && deptFilterCombo.getItems().contains(currentDept)) {
            deptFilterCombo.setValue(currentDept);
        } else {
            deptFilterCombo.setValue("All Departments");
        }
        
        // Update batch filter
        Set<String> batches = list.stream()
                .flatMap(opt -> opt.getBatchIds().stream())
                .collect(Collectors.toSet());
        
        String currentBatch = batchFilterCombo.getValue();
        batchFilterCombo.getItems().clear();
        batchFilterCombo.getItems().add("All Batches");
        
        List<Batch> allBatches = storage.loadBatches();
        for (String batchId : batches) {
            allBatches.stream()
                    .filter(b -> b.getId().equals(batchId))
                    .findFirst()
                    .ifPresent(b -> batchFilterCombo.getItems().add(b.getId() + " — " + b.getName()));
        }
        
        if (currentBatch != null && batchFilterCombo.getItems().contains(currentBatch)) {
            batchFilterCombo.setValue(currentBatch);
        } else {
            batchFilterCombo.setValue("All Batches");
        }
        
        applyFilters();
    }

    private String formatBatches(MergedClassOption opt) {
        List<Batch> batches = storage.loadBatches();
        List<String> batchIds = opt.getBatchIds();
        if (batchIds.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        // Mark primary batch (first one)
        String primaryId = batchIds.get(0);
        batches.stream().filter(b -> b.getId().equals(primaryId)).findFirst()
                .ifPresent(b -> sb.append("⭐ ").append(b.getName()).append(" (").append(b.getDepartmentId()).append(")"));
        
        // Add additional batches
        for (int i = 1; i < batchIds.size(); i++) {
            String bid = batchIds.get(i);
            batches.stream().filter(b -> b.getId().equals(bid)).findFirst()
                    .ifPresent(b -> sb.append(" + ").append(b.getName()).append(" (").append(b.getDepartmentId()).append(")"));
        }
        return sb.toString();
    }

    private String formatDepartments(MergedClassOption opt) {
        List<Batch> batches = storage.loadBatches();
        Set<String> depts = opt.getBatchIds().stream()
                .map(bid -> batches.stream().filter(b -> b.getId().equals(bid)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(Batch::getDepartmentId)
                .collect(Collectors.toSet());
        
        List<Department> allDepts = storage.loadDepartments();
        return depts.stream()
                .map(deptId -> allDepts.stream().filter(d -> d.getId().equals(deptId)).findFirst()
                        .map(Department::getName).orElse(deptId))
                .collect(Collectors.joining(", "));
    }

    private String formatCourses(MergedClassOption opt) {
        if (opt.getCourseCodes() == null || opt.getCourseCodes().isEmpty()) {
            return "All courses";
        }
        return String.join(", ", opt.getCourseCodes());
    }

    private String getTeacherName(String teacherId) {
        if (teacherId == null) return "";
        return storage.loadTeachers().stream()
                .filter(t -> teacherId.equals(t.getTeacherId()))
                .findFirst()
                .map(Teacher::getName)
                .orElse(teacherId);
    }

    private void generateRoutineForMerge(MergedClassOption opt) {
        if (opt == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generate Routine");
        confirm.setHeaderText("Generate routine entries for this 3-batch merged class?");
        confirm.setContentText("This will create routine entries for all 3 batches with the same teacher, room, day and time slot.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                List<RoutineEntry> routine = storage.loadRoutine();
                List<RoutineEntry> newEntries = new ArrayList<>();
                
                // Get current date and find next occurrence of the selected day
                java.time.LocalDate currentDate = java.time.LocalDate.now();
                java.time.DayOfWeek targetDay = convertToDayOfWeek(opt.getDay());
                
                // Find next occurrence of target day
                java.time.LocalDate date = currentDate;
                while (date.getDayOfWeek() != targetDay) {
                    date = date.plusDays(1);
                }
                
                for (String batchId : opt.getBatchIds()) {
                    RoutineEntry entry = new RoutineEntry();
                    entry.setId(UUID.randomUUID().toString());
                    entry.setBatchId(batchId);
                    entry.setTeacherId(opt.getTeacherId());
                    entry.setTeacherName(getTeacherName(opt.getTeacherId()));
                    entry.setRoomId(opt.getRoomNo());
                    entry.setDate(date);
                    
                    // Convert time slot string to LocalTime
                    java.time.LocalTime time = convertToLocalTime(opt.getTimeSlot());
                    entry.setTime(time);
                    
                    // Set course codes
                    if (opt.getCourseCodes() != null && !opt.getCourseCodes().isEmpty()) {
                        entry.setCourseId(opt.getCourseCodes().get(0));
                        entry.setCourseCode(opt.getCourseCodes().get(0));
                        if (opt.getCourseCodes().size() > 1) {
                            entry.setCourseTitle(String.join(", ", opt.getCourseCodes()));
                        }
                    }
                    
                    entry.setMerged(true);
                    entry.setBatchIds(opt.getBatchIds());
                    
                    newEntries.add(entry);
                }
                
                routine.addAll(newEntries);
                storage.saveRoutine(routine);
                
                new Alert(Alert.AlertType.INFORMATION, 
                    "✅ Routine entries generated for " + opt.getBatchIds().size() + " batches!\n" +
                    "Go to View Routine to see them.").showAndWait();
            }
        });
    }

    private java.time.DayOfWeek convertToDayOfWeek(String day) {
        switch (day) {
            case "Saturday": return java.time.DayOfWeek.SATURDAY;
            case "Sunday": return java.time.DayOfWeek.SUNDAY;
            case "Monday": return java.time.DayOfWeek.MONDAY;
            case "Tuesday": return java.time.DayOfWeek.TUESDAY;
            case "Wednesday": return java.time.DayOfWeek.WEDNESDAY;
            case "Thursday": return java.time.DayOfWeek.THURSDAY;
            case "Friday": return java.time.DayOfWeek.FRIDAY;
            default: return java.time.DayOfWeek.MONDAY;
        }
    }

    private java.time.LocalTime convertToLocalTime(String timeSlot) {
        switch (timeSlot) {
            case "8:30-9:50": return java.time.LocalTime.of(8, 30);
            case "10:00-11:20": return java.time.LocalTime.of(10, 0);
            case "11:30-12:50": return java.time.LocalTime.of(11, 30);
            case "1:30-2:50": return java.time.LocalTime.of(13, 30);
            case "3:00-4:20": return java.time.LocalTime.of(15, 0);
            case "4:30-5:50": return java.time.LocalTime.of(16, 30);
            default: return java.time.LocalTime.of(9, 0);
        }
    }

    private void showAddMergeDialog() {
        showMergeDialog(null);
    }

    private void showEditMergeDialog(MergedClassOption opt) {
        if (opt == null) return;
        showMergeDialog(opt);
    }

    private void showMergeDialog(MergedClassOption existing) {
        List<Batch> batches = storage.loadBatches();
        List<Teacher> teachers = storage.loadTeachers();
        List<Room> rooms = storage.loadRooms();
        List<Department> depts = storage.loadDepartments();

        if (batches.isEmpty() || teachers.isEmpty() || rooms.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Add batches, teachers and rooms first!").showAndWait();
            return;
        }

        // Debug: Print all courses from all batches
        System.out.println("\n=== COURSE DEBUG ===");
        Set<Course> allUniqueCourses = new HashSet<>();
        for (Batch b : batches) {
            System.out.println("Batch: " + b.getName() + " (" + b.getId() + ") - Dept: " + b.getDepartmentId());
            List<Course> batchCourses = b.getCourses();
            System.out.println("  Courses in this batch: " + batchCourses.size());
            allUniqueCourses.addAll(batchCourses);
            for (Course c : batchCourses) {
                System.out.println("    - " + c.getId() + ": " + c.getTitle() + " (" + c.getDepartmentId() + ", " + c.getCredit() + " cr)");
            }
        }
        System.out.println("Total unique courses across all batches: " + allUniqueCourses.size());
        System.out.println("========================\n");

        Dialog<MergedClassOption> d = new Dialog<>();
        d.setTitle(existing == null ? "New 3-Batch Merge" : "Edit 3-Batch Merge");
        d.getDialogPane().setPrefSize(900, 850);

        // ========== STEP 1: Select Primary Batch ==========
        Label step1Label = new Label("STEP 1: Select Primary Batch (Batch 1)");
        step1Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        
        ComboBox<String> primaryDeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        primaryDeptFilter.getItems().add(0, "All Departments");
        primaryDeptFilter.setValue("All Departments");
        primaryDeptFilter.setStyle(UIStyles.COMBO);
        primaryDeptFilter.setPrefWidth(280);

        ObservableList<Batch> batchObs = FXCollections.observableArrayList(batches);
        FilteredList<Batch> filteredPrimaryBatches = new FilteredList<>(batchObs, p -> true);
        
        primaryDeptFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || "All Departments".equals(newVal)) {
                filteredPrimaryBatches.setPredicate(b -> true);
            } else {
                String deptId = newVal.split(" — ")[0];
                filteredPrimaryBatches.setPredicate(b -> deptId.equals(b.getDepartmentId()));
            }
        });

        ComboBox<Batch> primaryBatchCombo = new ComboBox<>(filteredPrimaryBatches);
        primaryBatchCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Batch b) { 
                return b != null ? b.getName() + " (" + b.getDepartmentId() + ", " + b.getProgramType() + ") - " + b.getTotalStudents() + " students" : ""; 
            }
            public Batch fromString(String s) { return null; }
        });
        primaryBatchCombo.setStyle(UIStyles.COMBO);
        primaryBatchCombo.setPrefWidth(500);
        
        if (existing != null && !existing.getBatchIds().isEmpty()) {
            String primaryBatchId = existing.getBatchIds().get(0);
            batches.stream().filter(b -> b.getId().equals(primaryBatchId)).findFirst().ifPresent(primaryBatchCombo::setValue);
        }

        // ========== STEP 2: Select Additional Batch 2 ==========
        Label step2Label = new Label("STEP 2: Select Additional Batch 2");
        step2Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        step2Label.setPadding(new Insets(15, 0, 0, 0));

        ComboBox<String> batch2DeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        batch2DeptFilter.getItems().add(0, "All Departments");
        batch2DeptFilter.setValue("All Departments");
        batch2DeptFilter.setStyle(UIStyles.COMBO);
        batch2DeptFilter.setPrefWidth(280);

        FilteredList<Batch> filteredBatch2 = new FilteredList<>(batchObs, p -> true);
        
        batch2DeptFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || "All Departments".equals(newVal)) {
                filteredBatch2.setPredicate(b -> true);
            } else {
                String deptId = newVal.split(" — ")[0];
                filteredBatch2.setPredicate(b -> deptId.equals(b.getDepartmentId()));
            }
        });

        ComboBox<Batch> batch2Combo = new ComboBox<>(filteredBatch2);
        batch2Combo.setConverter(primaryBatchCombo.getConverter());
        batch2Combo.setStyle(UIStyles.COMBO);
        batch2Combo.setPrefWidth(500);
        
        if (existing != null && existing.getBatchIds().size() > 1) {
            String batch2Id = existing.getBatchIds().get(1);
            batches.stream().filter(b -> b.getId().equals(batch2Id)).findFirst().ifPresent(batch2Combo::setValue);
        }

        // ========== STEP 3: Select Additional Batch 3 ==========
        Label step3Label = new Label("STEP 3: Select Additional Batch 3");
        step3Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        step3Label.setPadding(new Insets(15, 0, 0, 0));

        ComboBox<String> batch3DeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        batch3DeptFilter.getItems().add(0, "All Departments");
        batch3DeptFilter.setValue("All Departments");
        batch3DeptFilter.setStyle(UIStyles.COMBO);
        batch3DeptFilter.setPrefWidth(280);

        FilteredList<Batch> filteredBatch3 = new FilteredList<>(batchObs, p -> true);
        
        batch3DeptFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || "All Departments".equals(newVal)) {
                filteredBatch3.setPredicate(b -> true);
            } else {
                String deptId = newVal.split(" — ")[0];
                filteredBatch3.setPredicate(b -> deptId.equals(b.getDepartmentId()));
            }
        });

        ComboBox<Batch> batch3Combo = new ComboBox<>(filteredBatch3);
        batch3Combo.setConverter(primaryBatchCombo.getConverter());
        batch3Combo.setStyle(UIStyles.COMBO);
        batch3Combo.setPrefWidth(500);
        
        if (existing != null && existing.getBatchIds().size() > 2) {
            String batch3Id = existing.getBatchIds().get(2);
            batches.stream().filter(b -> b.getId().equals(batch3Id)).findFirst().ifPresent(batch3Combo::setValue);
        }

        // ========== STEP 4: Select Common Details with Filters ==========
        Label step4Label = new Label("STEP 4: Select Common Details");
        step4Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        step4Label.setPadding(new Insets(15, 0, 5, 0));

        // Teacher selection with department filter
        Label teacherFilterLabel = new Label("Filter Teachers by Dept:");
        teacherFilterLabel.setStyle(UIStyles.LABEL_FORM);
        
        ComboBox<String> teacherDeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        teacherDeptFilter.getItems().add(0, "All Departments");
        teacherDeptFilter.setValue("All Departments");
        teacherDeptFilter.setStyle(UIStyles.COMBO);
        teacherDeptFilter.setPrefWidth(280);

        ObservableList<Teacher> teacherObs = FXCollections.observableArrayList(teachers);
        FilteredList<Teacher> filteredTeachers = new FilteredList<>(teacherObs, p -> true);
        
        teacherDeptFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || "All Departments".equals(newVal)) {
                filteredTeachers.setPredicate(t -> true);
            } else {
                String deptId = newVal.split(" — ")[0];
                filteredTeachers.setPredicate(t -> deptId.equals(t.getDepartmentId()));
            }
        });

        ComboBox<Teacher> teacherCombo = new ComboBox<>(filteredTeachers);
        teacherCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Teacher t) { 
                return t != null ? t.getTeacherId() + " — " + t.getName() + " (" + t.getDepartmentId() + ")" : ""; 
            }
            public Teacher fromString(String s) { return null; }
        });
        teacherCombo.setStyle(UIStyles.COMBO);
        teacherCombo.setPrefWidth(500);
        
        if (existing != null) {
            teachers.stream().filter(t -> t.getTeacherId().equals(existing.getTeacherId())).findFirst().ifPresent(teacherCombo::setValue);
        }

        // Room selection with department filter
        Label roomFilterLabel = new Label("Filter Rooms by Dept:");
        roomFilterLabel.setStyle(UIStyles.LABEL_FORM);
        
        ComboBox<String> roomDeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        roomDeptFilter.getItems().add(0, "All Departments");
        roomDeptFilter.setValue("All Departments");
        roomDeptFilter.setStyle(UIStyles.COMBO);
        roomDeptFilter.setPrefWidth(280);

        ObservableList<Room> roomObs = FXCollections.observableArrayList(rooms);
        FilteredList<Room> filteredRooms = new FilteredList<>(roomObs, p -> true);
        
        roomDeptFilter.valueProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || "All Departments".equals(newVal)) {
                filteredRooms.setPredicate(r -> true);
            } else {
                String deptId = newVal.split(" — ")[0];
                filteredRooms.setPredicate(r -> deptId.equals(r.getDepartmentId()));
            }
        });

        ComboBox<Room> roomCombo = new ComboBox<>(filteredRooms);
        roomCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Room r) { 
                return r != null ? r.getRoomNo() + " (" + r.getRoomType() + ", cap " + r.getCapacity() + ")" + 
                    (r.getDepartmentId() != null ? " - " + r.getDepartmentId() : "") : ""; 
            }
            public Room fromString(String s) { return null; }
        });
        roomCombo.setStyle(UIStyles.COMBO);
        roomCombo.setPrefWidth(500);
        
        if (existing != null) {
            rooms.stream().filter(r -> r.getRoomNo().equals(existing.getRoomNo())).findFirst().ifPresent(roomCombo::setValue);
        }

        // Day selection
        Label dayLabel = new Label("Day:");
        dayLabel.setStyle(UIStyles.LABEL_FORM);
        
        ComboBox<String> dayCombo = new ComboBox<>(FXCollections.observableArrayList("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        dayCombo.setStyle(UIStyles.COMBO);
        dayCombo.setPrefWidth(200);
        if (existing != null) dayCombo.setValue(existing.getDay());

        // Time slot selection
        Label slotLabel = new Label("Time Slot:");
        slotLabel.setStyle(UIStyles.LABEL_FORM);
        
        ComboBox<String> slotCombo = new ComboBox<>(FXCollections.observableArrayList(TimeRules.TEACHING_SLOTS));
        slotCombo.setStyle(UIStyles.COMBO);
        slotCombo.setPrefWidth(200);
        if (existing != null) slotCombo.setValue(existing.getTimeSlot());

        // ========== STEP 5: Select Courses with Department Filter ==========
        Label step5Label = new Label("STEP 5: Select Courses");
        step5Label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        step5Label.setPadding(new Insets(15, 0, 5, 0));

        // Course input method toggle
        ToggleGroup courseInputType = new ToggleGroup();
        RadioButton selectCoursesRadio = new RadioButton("Select from List");
        selectCoursesRadio.setToggleGroup(courseInputType);
        selectCoursesRadio.setSelected(true);
        
        RadioButton manualCoursesRadio = new RadioButton("Enter Manually");
        manualCoursesRadio.setToggleGroup(courseInputType);
        
        HBox radioBox = new HBox(20, selectCoursesRadio, manualCoursesRadio);
        radioBox.setPadding(new Insets(5, 0, 10, 0));

        // Course selection with department filter
        VBox courseSelectBox = new VBox(10);
        
        Label courseFilterLabel = new Label("Filter Courses by Dept:");
        courseFilterLabel.setStyle(UIStyles.LABEL_FORM);
        
        ComboBox<String> courseDeptFilter = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        courseDeptFilter.getItems().add(0, "All Departments");
        courseDeptFilter.setValue("All Departments");
        courseDeptFilter.setStyle(UIStyles.COMBO);
        courseDeptFilter.setPrefWidth(280);

        // Course list
        VBox courseCheckBox = new VBox(6);
        courseCheckBox.setPadding(new Insets(8));
        courseCheckBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8;");
        List<CheckBox> courseChecks = new ArrayList<>();
        
        Runnable updateCourseList = () -> {
            courseCheckBox.getChildren().clear();
            courseChecks.clear();
            
            String selectedDept = courseDeptFilter.getValue();
            
            // Get all unique courses from all batches
            Set<Course> uniqueCourses = new HashSet<>();
            for (Batch batch : batches) {
                uniqueCourses.addAll(batch.getCourses());
            }
            
            // Convert to list and sort
            List<Course> allCourses = new ArrayList<>(uniqueCourses);
            allCourses.sort(Comparator.comparing(Course::getId));
            
            System.out.println("Total unique courses across all batches: " + allCourses.size());
            
            for (Course c : allCourses) {
                // Apply department filter
                if (selectedDept != null && !selectedDept.equals("All Departments")) {
                    String deptId = selectedDept.split(" — ")[0];
                    if (!deptId.equals(c.getDepartmentId())) continue;
                }
                
                CheckBox cb = new CheckBox(c.getId() + " — " + c.getTitle() + " (" + c.getCredit() + " cr) - " + c.getDepartmentId());
                cb.setUserData(c.getId());
                cb.setStyle("-fx-font-size: 12px; -fx-padding: 2 0;");
                
                if (existing != null && existing.getCourseCodes() != null && existing.getCourseCodes().contains(c.getId())) {
                    cb.setSelected(true);
                }
                courseChecks.add(cb);
                courseCheckBox.getChildren().add(cb);
            }
            
            System.out.println("Courses displayed after filter: " + courseChecks.size());
            
            // If no courses found, show a message
            if (courseChecks.isEmpty()) {
                Label noCoursesLabel = new Label("No courses found for selected department");
                noCoursesLabel.setStyle("-fx-text-fill: #999; -fx-padding: 10;");
                courseCheckBox.getChildren().add(noCoursesLabel);
            }
        };
        
        // Initial load
        updateCourseList.run();
        
        // Add listener for filter changes
        courseDeptFilter.valueProperty().addListener((obs, old, newVal) -> {
            System.out.println("Course filter changed to: " + newVal);
            updateCourseList.run();
        });

        ScrollPane courseScroll = new ScrollPane(courseCheckBox);
        courseScroll.setFitToWidth(true);
        courseScroll.setPrefHeight(200);
        courseScroll.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8; -fx-background-color: white;");

        courseSelectBox.getChildren().addAll(courseFilterLabel, courseDeptFilter, courseScroll);

        // Manual course entry
        TextField manualCourseField = new TextField();
        manualCourseField.setPromptText("Enter course codes separated by commas (e.g., CSE1101, EEE1203, MATH2101)");
        manualCourseField.setStyle(UIStyles.INPUT);
        manualCourseField.setDisable(true);
        manualCourseField.setVisible(false);

        // Toggle between selection and manual entry
        selectCoursesRadio.setOnAction(e -> {
            courseSelectBox.setDisable(false);
            courseSelectBox.setVisible(true);
            manualCourseField.setDisable(true);
            manualCourseField.setVisible(false);
        });
        
        manualCoursesRadio.setOnAction(e -> {
            courseSelectBox.setDisable(true);
            courseSelectBox.setVisible(false);
            manualCourseField.setDisable(false);
            manualCourseField.setVisible(true);
        });

        VBox courseInputBox = new VBox(10, radioBox, courseSelectBox, manualCourseField);

        // ========== FORM LAYOUT ==========
        GridPane form = new GridPane();
        form.setHgap(16); 
        form.setVgap(14); 
        form.setPadding(new Insets(16));
        
        int row = 0;
        
        // Step 1
        form.add(step1Label, 0, row++, 2, 1);
        form.add(lbl("Filter by Dept:"), 0, row); 
        form.add(primaryDeptFilter, 1, row++);
        form.add(lbl("Primary Batch:"), 0, row); 
        form.add(primaryBatchCombo, 1, row++);
        
        // Step 2
        form.add(step2Label, 0, row++, 2, 1);
        form.add(lbl("Filter by Dept:"), 0, row); 
        form.add(batch2DeptFilter, 1, row++);
        form.add(lbl("Batch 2:"), 0, row); 
        form.add(batch2Combo, 1, row++);
        
        // Step 3
        form.add(step3Label, 0, row++, 2, 1);
        form.add(lbl("Filter by Dept:"), 0, row); 
        form.add(batch3DeptFilter, 1, row++);
        form.add(lbl("Batch 3:"), 0, row); 
        form.add(batch3Combo, 1, row++);
        
        // Step 4 - Teacher with filter
        form.add(step4Label, 0, row++, 2, 1);
        form.add(lbl("Filter Teachers:"), 0, row); 
        form.add(teacherDeptFilter, 1, row++);
        form.add(lbl("Teacher:"), 0, row); 
        form.add(teacherCombo, 1, row++);
        form.add(lbl("Filter Rooms:"), 0, row); 
        form.add(roomDeptFilter, 1, row++);
        form.add(lbl("Room:"), 0, row); 
        form.add(roomCombo, 1, row++);
        form.add(lbl("Day:"), 0, row); 
        form.add(dayCombo, 1, row++);
        form.add(lbl("Time Slot:"), 0, row); 
        form.add(slotCombo, 1, row++);
        
        // Step 5 - Courses with filter
        form.add(step5Label, 0, row++, 2, 1);
        form.add(courseInputBox, 0, row++, 2, 1);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setPrefHeight(750);
        formScroll.setStyle("-fx-background-color: transparent;");
        
        d.getDialogPane().setContent(formScroll);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            
            Batch primaryBatch = primaryBatchCombo.getValue();
            Batch batch2 = batch2Combo.getValue();
            Batch batch3 = batch3Combo.getValue();
            
            if (primaryBatch == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a primary batch!").showAndWait();
                return null;
            }
            
            if (batch2 == null) {
                new Alert(Alert.AlertType.WARNING, "Please select Batch 2!").showAndWait();
                return null;
            }
            
            if (batch3 == null) {
                new Alert(Alert.AlertType.WARNING, "Please select Batch 3!").showAndWait();
                return null;
            }
            
            // Check for duplicate batches
            Set<String> batchIds = new HashSet<>();
            batchIds.add(primaryBatch.getId());
            batchIds.add(batch2.getId());
            batchIds.add(batch3.getId());
            
            if (batchIds.size() < 3) {
                new Alert(Alert.AlertType.WARNING, "Please select three DIFFERENT batches!").showAndWait();
                return null;
            }
            
            Teacher t = teacherCombo.getValue();
            Room r = roomCombo.getValue();
            if (t == null || r == null) {
                new Alert(Alert.AlertType.WARNING, "Please select teacher and room!").showAndWait();
                return null;
            }

            // Get selected courses
            List<String> selectedCourses = new ArrayList<>();
            
            if (selectCoursesRadio.isSelected()) {
                for (CheckBox cb : courseChecks) {
                    if (cb.isSelected()) {
                        selectedCourses.add(cb.getUserData().toString());
                    }
                }
            } else {
                selectedCourses = Arrays.stream(manualCourseField.getText().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
            
            if (selectedCourses.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please select at least one course!").showAndWait();
                return null;
            }

            MergedClassOption opt = existing != null ? existing : new MergedClassOption();
            if (existing == null) opt.setId(UUID.randomUUID().toString());
            
            opt.setBatchIds(new ArrayList<>(batchIds));
            opt.setCourseCodes(selectedCourses);
            opt.setTeacherId(t.getTeacherId());
            opt.setRoomNo(r.getRoomNo());
            opt.setDay(dayCombo.getValue());
            opt.setTimeSlot(slotCombo.getValue());
            
            return opt;
        });
        
        d.showAndWait().ifPresent(opt -> {
            if (opt != null) {
                if (existing == null) list.add(opt);
                storage.saveMergedOptions(new ArrayList<>(list));
                table.refresh();
                updateFilters();
            }
        });
    }

    private void deleteMerge(MergedClassOption opt) {
        if (opt == null) return;
        
        // Check if this merge is used in routine
        boolean hasRoutine = storage.loadRoutine().stream()
                .anyMatch(r -> r.isMerged() && r.getBatchIds() != null && 
                        r.getBatchIds().equals(opt.getBatchIds()) &&
                        opt.getDay().equals(r.getDay()) &&
                        opt.getTimeSlot().equals(r.getTimeSlot()));
        
        if (hasRoutine) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Merged Class");
            alert.setHeaderText("This merged class has routine entries!");
            alert.setContentText("Deleting it will also remove associated routine entries. Continue?");
            
            alert.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    // Remove associated routine entries
                    List<RoutineEntry> routine = storage.loadRoutine();
                    routine.removeIf(r -> r.isMerged() && r.getBatchIds() != null && 
                            r.getBatchIds().equals(opt.getBatchIds()) &&
                            opt.getDay().equals(r.getDay()) &&
                            opt.getTimeSlot().equals(r.getTimeSlot()));
                    storage.saveRoutine(routine);
                    
                    list.remove(opt);
                    storage.saveMergedOptions(new ArrayList<>(list));
                    updateFilters();
                }
            });
        } else {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete this merged class?");
            c.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) { 
                    list.remove(opt); 
                    storage.saveMergedOptions(new ArrayList<>(list));
                    updateFilters();
                }
            });
        }
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle(UIStyles.LABEL_FORM);
        return l;
    }

    private void refresh() {
        list.clear();
        list.addAll(storage.loadMergedOptions());
    }

    public VBox getRoot() { return root; }
}