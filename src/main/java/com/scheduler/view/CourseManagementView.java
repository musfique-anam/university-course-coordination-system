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

import java.util.*;
import java.util.stream.Collectors;

public class CourseManagementView {
    private final DatabaseStorage storage;
    private final ObservableList<Course> masterList = FXCollections.observableArrayList();
    private final FilteredList<Course> filteredList;
    private final TableView<Course> table = new TableView<>();
    private final VBox root;
    private ComboBox<String> deptFilterCombo;
    private ComboBox<String> batchFilterCombo;
    private ComboBox<String> programFilterCombo;

    public CourseManagementView(DatabaseStorage storage) {
        this.storage = storage;
        this.filteredList = new FilteredList<>(masterList, p -> true);
        this.root = build();
        refresh();
    }

    private VBox build() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(20));
        main.setStyle(UIStyles.BG_MAIN);

        // Title Bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("📖  Course Management");
        title.setStyle(UIStyles.TITLE);

        // Filter Section
        Label filterLabel = new Label("Filters:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64;");

        deptFilterCombo = new ComboBox<>();
        deptFilterCombo.getItems().add("All Departments");
        deptFilterCombo.setValue("All Departments");
        deptFilterCombo.setStyle(UIStyles.COMBO);
        deptFilterCombo.setPrefWidth(180);
        deptFilterCombo.setOnAction(e -> {
            updateBatchFilter();
            applyFilters();
        });

        batchFilterCombo = new ComboBox<>();
        batchFilterCombo.getItems().add("All Batches");
        batchFilterCombo.setValue("All Batches");
        batchFilterCombo.setStyle(UIStyles.COMBO);
        batchFilterCombo.setPrefWidth(180);
        batchFilterCombo.setOnAction(e -> applyFilters());

        programFilterCombo = new ComboBox<>();
        programFilterCombo.getItems().addAll("All Programs", "HSC", "DIPLOMA");
        programFilterCombo.setValue("All Programs");
        programFilterCombo.setStyle(UIStyles.COMBO);
        programFilterCombo.setPrefWidth(150);
        programFilterCombo.setOnAction(e -> applyFilters());

        Button addBtn = new Button("➕ Add New Course");
        addBtn.setStyle(UIStyles.BTN_SUCCESS);
        addBtn.setOnAction(e -> showAddCourseDialog());

        Button assignBtn = new Button("📌 Assign Courses to Batches");
        assignBtn.setStyle(UIStyles.BTN_PRIMARY);
        assignBtn.setOnAction(e -> showAssignCoursesDialog());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(UIStyles.BTN_BACK);
        refreshBtn.setOnAction(e -> refresh());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, addBtn, assignBtn, refreshBtn);
        
        HBox filterBox = new HBox(10, filterLabel, deptFilterCombo, batchFilterCombo, programFilterCombo, sp, buttonBox);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        titleBar.getChildren().addAll(title, filterBox);

        // Table
        setupTable();

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        main.getChildren().addAll(titleBar, tableCard);
        updateDepartmentFilter();
        return main;
    }

    private void setupTable() {
        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        codeCol.setPrefWidth(120);

        TableColumn<Course, String> titleCol = new TableColumn<>("Course Title");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        titleCol.setPrefWidth(250);

        TableColumn<Course, String> creditCol = new TableColumn<>("Credit");
        creditCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCredit())));
        creditCol.setPrefWidth(80);

        TableColumn<Course, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseType().name()));
        typeCol.setPrefWidth(100);

        TableColumn<Course, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartmentId()));
        deptCol.setPrefWidth(120);

        TableColumn<Course, String> programCol = new TableColumn<>("Program");
        programCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProgramType().name()));
        programCol.setPrefWidth(100);

        TableColumn<Course, String> batchCol = new TableColumn<>("Assigned Batches");
        batchCol.setCellValueFactory(c -> {
            String batchId = c.getValue().getBatchId();
            if (batchId == null || batchId.isEmpty()) {
                return new SimpleStringProperty("Not Assigned");
            }
            
            // Handle multiple batch IDs (comma-separated)
            String[] batchIds = batchId.split(",");
            List<String> batchNames = new ArrayList<>();
            List<Batch> allBatches = storage.loadBatches();
            
            for (String id : batchIds) {
                String trimmedId = id.trim();
                if (!trimmedId.isEmpty()) {
                    Optional<Batch> batchOpt = allBatches.stream()
                            .filter(b -> trimmedId.equals(b.getId()))
                            .findFirst();
                    batchOpt.ifPresent(b -> batchNames.add(b.getName() + " (" + b.getDepartmentId() + ")"));
                }
            }
            
            return batchNames.isEmpty() ? new SimpleStringProperty("Unknown") : 
                   new SimpleStringProperty(String.join(", ", batchNames));
        });
        batchCol.setPrefWidth(250);

        TableColumn<Course, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(200);
        actCol.setCellFactory(cc -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button assignBtn = new Button("📌 Assign");

            {
                editBtn.setStyle(UIStyles.BTN_WARNING + "-fx-font-size: 11px; -fx-padding: 5 10;");
                assignBtn.setStyle(UIStyles.BTN_PRIMARY + "-fx-font-size: 11px; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> {
                    Course course = getTableRow().getItem();
                    if (course != null) editCourse(course);
                });
                
                assignBtn.setOnAction(e -> {
                    Course course = getTableRow().getItem();
                    if (course != null) assignCourseToBatches(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, assignBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(codeCol, titleCol, creditCol, typeCol, deptCol, programCol, batchCol, actCol);
        table.setItems(filteredList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(500);
    }

    private void applyFilters() {
        if (filteredList == null) return;

        String selectedDept = deptFilterCombo.getValue();
        String selectedBatch = batchFilterCombo.getValue();
        String selectedProgram = programFilterCombo.getValue();

        filteredList.setPredicate(course -> {
            // Department filter
            if (selectedDept != null && !selectedDept.equals("All Departments")) {
                String deptId = selectedDept.split(" — ")[0];
                if (!deptId.equals(course.getDepartmentId())) return false;
            }

            // Batch filter
            if (selectedBatch != null && !selectedBatch.equals("All Batches")) {
                String batchId = selectedBatch.split(" — ")[0];
                String courseBatchIds = course.getBatchId();
                if (courseBatchIds == null || !courseBatchIds.contains(batchId)) return false;
            }

            // Program filter
            if (selectedProgram != null && !selectedProgram.equals("All Programs")) {
                if (course.getProgramType() == null || !selectedProgram.equals(course.getProgramType().name())) return false;
            }

            return true;
        });
    }

    private void updateDepartmentFilter() {
        Set<String> depts = masterList.stream()
                .map(Course::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String currentDept = deptFilterCombo.getValue();
        deptFilterCombo.getItems().clear();
        deptFilterCombo.getItems().add("All Departments");

        List<Department> allDepts = storage.loadDepartments();
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
        
        updateBatchFilter();
    }

    private void updateBatchFilter() {
        String selectedDept = deptFilterCombo.getValue();
        String currentBatch = batchFilterCombo.getValue();
        
        batchFilterCombo.getItems().clear();
        batchFilterCombo.getItems().add("All Batches");

        List<Batch> allBatches = storage.loadBatches();
        
        // Filter batches by selected department
        List<Batch> filteredBatches = allBatches;
        if (selectedDept != null && !selectedDept.equals("All Departments")) {
            String deptId = selectedDept.split(" — ")[0];
            filteredBatches = allBatches.stream()
                    .filter(b -> deptId.equals(b.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        for (Batch batch : filteredBatches) {
            batchFilterCombo.getItems().add(batch.getId() + " — " + batch.getName());
        }

        if (currentBatch != null && batchFilterCombo.getItems().contains(currentBatch)) {
            batchFilterCombo.setValue(currentBatch);
        } else {
            batchFilterCombo.setValue("All Batches");
        }
    }

    private void showAddCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.getDialogPane().setPrefSize(500, 500);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        // Course Code
        TextField codeField = new TextField();
        codeField.setPromptText("e.g., CSE1101");
        codeField.setStyle(UIStyles.INPUT);

        // Course Title
        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");
        titleField.setStyle(UIStyles.INPUT);

        // Credit
        Spinner<Integer> creditSpinner = new Spinner<>(1, 4, 3);
        creditSpinner.setEditable(true);
        creditSpinner.setPrefWidth(100);

        // Course Type
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));
        typeCombo.setValue("THEORY");
        typeCombo.setStyle(UIStyles.COMBO);

        // Department
        ComboBox<String> deptCombo = new ComboBox<>();
        storage.loadDepartments().forEach(d -> deptCombo.getItems().add(d.getId() + " — " + d.getName()));
        deptCombo.setPromptText("Select Department");
        deptCombo.setStyle(UIStyles.COMBO);

        // Program Type
        ComboBox<String> programCombo = new ComboBox<>(FXCollections.observableArrayList("HSC", "DIPLOMA"));
        programCombo.setValue("DIPLOMA");
        programCombo.setStyle(UIStyles.COMBO);

        int row = 0;
        form.add(new Label("Course Code *:"), 0, row);
        form.add(codeField, 1, row++);
        form.add(new Label("Course Title *:"), 0, row);
        form.add(titleField, 1, row++);
        form.add(new Label("Credit:"), 0, row);
        form.add(creditSpinner, 1, row++);
        form.add(new Label("Course Type:"), 0, row);
        form.add(typeCombo, 1, row++);
        form.add(new Label("Department *:"), 0, row);
        form.add(deptCombo, 1, row++);
        form.add(new Label("Program Type:"), 0, row);
        form.add(programCombo, 1, row++);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String code = codeField.getText().trim();
                String title = titleField.getText().trim();
                String dept = deptCombo.getValue();

                if (code.isEmpty() || title.isEmpty() || dept == null) {
                    new Alert(Alert.AlertType.WARNING, "Please fill all required fields!").showAndWait();
                    return null;
                }

                // Check if course already exists
                boolean exists = masterList.stream().anyMatch(c -> c.getId().equals(code));
                if (exists) {
                    new Alert(Alert.AlertType.WARNING, "Course code already exists!").showAndWait();
                    return null;
                }

                String deptId = dept.split(" — ")[0];
                ProgramType program = "HSC".equals(programCombo.getValue()) ? ProgramType.HSC : ProgramType.DIPLOMA;
                CourseType courseType = "LAB".equals(typeCombo.getValue()) ? CourseType.LAB : CourseType.THEORY;

                return new Course(
                    code,
                    title,
                    creditSpinner.getValue(),
                    courseType,
                    deptId,
                    program,
                    null // No batch assigned yet
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            // Save the course to all batches? For now, just add to master list
            // You might want to implement a method to add course to storage
            masterList.add(course);
            
            // For now, we need to save it to some storage
            // This depends on your storage implementation
            // storage.saveCourse(course);
            
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Course added successfully!").showAndWait();
        });
    }

    private void editCourse(Course course) {
        if (course == null) return;

        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course: " + course.getId());
        dialog.getDialogPane().setPrefSize(500, 400);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField codeField = new TextField(course.getId());
        codeField.setEditable(false);
        codeField.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField titleField = new TextField(course.getTitle());
        titleField.setStyle(UIStyles.INPUT);
        
        Spinner<Integer> creditSpinner = new Spinner<>(1, 4, course.getCredit());
        creditSpinner.setEditable(true);
        creditSpinner.setPrefWidth(100);
        
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));
        typeCombo.setValue(course.getCourseType().name());
        typeCombo.setStyle(UIStyles.COMBO);

        int row = 0;
        form.add(new Label("Course Code:"), 0, row);
        form.add(codeField, 1, row++);
        form.add(new Label("Course Title:"), 0, row);
        form.add(titleField, 1, row++);
        form.add(new Label("Credit:"), 0, row);
        form.add(creditSpinner, 1, row++);
        form.add(new Label("Course Type:"), 0, row);
        form.add(typeCombo, 1, row++);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                course.setTitle(titleField.getText());
                course.setCredit(creditSpinner.getValue());
                course.setCourseType("LAB".equals(typeCombo.getValue()) ? CourseType.LAB : CourseType.THEORY);
                return course;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            updateCourseInAllBatches(c);
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Course updated successfully!").showAndWait();
        });
    }

    private void assignCourseToBatches(Course course) {
        if (course == null) return;

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Assign Course to Multiple Batches");
        dialog.getDialogPane().setPrefSize(700, 600);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label courseLabel = new Label("Course: " + course.getId() + " - " + course.getTitle());
        courseLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565C0;");

        // Department filter for batches (multi-department support)
        Label deptFilterLabel = new Label("Filter Batches by Department:");
        deptFilterLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All Departments");
        storage.loadDepartments().forEach(d -> deptFilter.getItems().add(d.getId() + " — " + d.getName()));
        deptFilter.setValue("All Departments");
        deptFilter.setStyle(UIStyles.COMBO);

        // Batch selection with checkboxes
        Label batchLabel = new Label("Select Batches to Assign (can select from multiple depts):");
        batchLabel.setStyle("-fx-font-weight: bold;");

        VBox batchCheckBox = new VBox(5);
        batchCheckBox.setPadding(new Insets(10));
        batchCheckBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8;");
        
        List<CheckBox> batchChecks = new ArrayList<>();
        List<Batch> allBatches = storage.loadBatches();
        
        // Get currently assigned batches
        Set<String> assignedBatches = new HashSet<>();
        if (course.getBatchId() != null && !course.getBatchId().isEmpty()) {
            String[] batchIds = course.getBatchId().split(",");
            for (String id : batchIds) {
                assignedBatches.add(id.trim());
            }
        }

        // Function to update batch list based on department filter
        Runnable updateBatchList = () -> {
            batchCheckBox.getChildren().clear();
            batchChecks.clear();
            
            String selectedDept = deptFilter.getValue();
            String deptId = null;
            if (selectedDept != null && !selectedDept.equals("All Departments")) {
                deptId = selectedDept.split(" — ")[0];
            }
            
            for (Batch batch : allBatches) {
                // Apply department filter
                if (deptId != null && !deptId.equals(batch.getDepartmentId())) {
                    continue;
                }
                
                CheckBox cb = new CheckBox(batch.getName() + " (" + batch.getDepartmentId() + ") - " + batch.getProgramType());
                cb.setUserData(batch.getId());
                
                // Check if already assigned
                if (assignedBatches.contains(batch.getId())) {
                    cb.setSelected(true);
                }
                
                batchChecks.add(cb);
                batchCheckBox.getChildren().add(cb);
            }
            
            // Show message if no batches
            if (batchChecks.isEmpty()) {
                Label noBatchesLabel = new Label("No batches found for selected department");
                noBatchesLabel.setStyle("-fx-text-fill: #999; -fx-padding: 10;");
                batchCheckBox.getChildren().add(noBatchesLabel);
            }
        };
        
        updateBatchList.run();
        
        deptFilter.valueProperty().addListener((obs, old, newVal) -> updateBatchList.run());

        ScrollPane batchScroll = new ScrollPane(batchCheckBox);
        batchScroll.setFitToWidth(true);
        batchScroll.setPrefHeight(250);
        batchScroll.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8;");

        // Select/Deselect buttons
        HBox buttonBox = new HBox(10);
        Button selectAllBtn = new Button("Select All");
        selectAllBtn.setStyle(UIStyles.BTN_PRIMARY);
        selectAllBtn.setOnAction(e -> batchChecks.forEach(cb -> cb.setSelected(true)));
        
        Button clearAllBtn = new Button("Clear All");
        clearAllBtn.setStyle(UIStyles.BTN_WARNING);
        clearAllBtn.setOnAction(e -> batchChecks.forEach(cb -> cb.setSelected(false)));
        
        buttonBox.getChildren().addAll(selectAllBtn, clearAllBtn);

        // Show current assignment info
        Label currentAssignmentLabel = new Label();
        if (!assignedBatches.isEmpty()) {
            List<String> batchNames = new ArrayList<>();
            for (String batchId : assignedBatches) {
                allBatches.stream()
                        .filter(b -> b.getId().equals(batchId))
                        .findFirst()
                        .ifPresent(b -> batchNames.add(b.getName() + " (" + b.getDepartmentId() + ")"));
            }
            currentAssignmentLabel.setText("Currently assigned to: " + String.join(", ", batchNames));
            currentAssignmentLabel.setStyle("-fx-text-fill: #4CAF50; -fx-padding: 5 0 0 0;");
        }

        // Assignment mode
        Label modeLabel = new Label("Assignment Mode:");
        modeLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton addMode = new RadioButton("Add to existing assignments");
        addMode.setToggleGroup(modeGroup);
        addMode.setSelected(true);
        
        RadioButton replaceMode = new RadioButton("Replace existing assignments");
        replaceMode.setToggleGroup(modeGroup);
        
        HBox modeBox = new HBox(20, addMode, replaceMode);

        content.getChildren().addAll(
            courseLabel,
            currentAssignmentLabel,
            new Separator(),
            deptFilterLabel, deptFilter,
            batchLabel, batchScroll,
            buttonBox,
            modeLabel, modeBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<String> selectedBatchIds = batchChecks.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> cb.getUserData().toString())
                        .collect(Collectors.toList());
                
                if (selectedBatchIds.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please select at least one batch!").showAndWait();
                    return null;
                }
                return selectedBatchIds;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(batchIds -> {
            boolean isReplaceMode = replaceMode.isSelected();
            
            Set<String> currentBatches = new HashSet<>();
            if (!isReplaceMode && course.getBatchId() != null && !course.getBatchId().isEmpty()) {
                String[] ids = course.getBatchId().split(",");
                for (String id : ids) {
                    currentBatches.add(id.trim());
                }
            }
            
            // Add new batches
            currentBatches.addAll(batchIds);
            
            // Update course
            course.setBatchId(String.join(",", currentBatches));
            updateCourseInAllBatches(course);
            
            // Force a complete refresh of the data
            refresh();
            
            new Alert(Alert.AlertType.INFORMATION, 
                "✅ Course assigned to " + batchIds.size() + " batches successfully!").showAndWait();
        });
    }

    private void updateCourseInAllBatches(Course course) {
        List<Batch> batches = storage.loadBatches();
        String courseId = course.getId();
        
        // Get all batch IDs where this course should be present
        Set<String> targetBatchIds = new HashSet<>();
        if (course.getBatchId() != null && !course.getBatchId().isEmpty()) {
            String[] ids = course.getBatchId().split(",");
            for (String id : ids) {
                targetBatchIds.add(id.trim());
            }
        }
        
        System.out.println("Updating course " + courseId + " for batches: " + targetBatchIds);
        
        boolean changes = false;
        
        for (Batch batch : batches) {
            List<Course> batchCourses = batch.getCourses();
            if (batchCourses == null) {
                batchCourses = new ArrayList<>();
                batch.setCourses(batchCourses);
            }
            
            boolean courseExists = false;
            int courseIndex = -1;
            
            // Check if course exists in this batch
            for (int i = 0; i < batchCourses.size(); i++) {
                Course c = batchCourses.get(i);
                if (c.getId().equals(courseId)) {
                    courseExists = true;
                    courseIndex = i;
                    break;
                }
            }
            
            // If this batch should have the course
            if (targetBatchIds.contains(batch.getId())) {
                if (courseExists) {
                    // Update existing course
                    Course updatedCourse = new Course(
                        course.getId(),
                        course.getTitle(),
                        course.getCredit(),
                        course.getCourseType(),
                        course.getDepartmentId(),
                        course.getProgramType(),
                        batch.getId()
                    );
                    batchCourses.set(courseIndex, updatedCourse);
                    System.out.println("  Updated course in batch: " + batch.getName());
                } else {
                    // Add new course
                    Course newCourse = new Course(
                        course.getId(),
                        course.getTitle(),
                        course.getCredit(),
                        course.getCourseType(),
                        course.getDepartmentId(),
                        course.getProgramType(),
                        batch.getId()
                    );
                    batchCourses.add(newCourse);
                    System.out.println("  Added course to batch: " + batch.getName());
                }
                changes = true;
            } else {
                // This batch should NOT have the course
                if (courseExists) {
                    // Remove course from this batch
                    batchCourses.remove(courseIndex);
                    System.out.println("  Removed course from batch: " + batch.getName());
                    changes = true;
                }
            }
        }
        
        if (changes) {
            storage.saveBatches(batches);
            System.out.println("✅ Changes saved successfully");
        }
    }

    private void showAssignCoursesDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Multiple Courses to Multiple Batches");
        dialog.getDialogPane().setPrefSize(900, 800);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Batch Selection Section
        Label batchSectionLabel = new Label("STEP 1: Select Target Batches (can select from multiple depts)");
        batchSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        ComboBox<String> batchDeptFilter = new ComboBox<>();
        batchDeptFilter.getItems().add("All Departments");
        storage.loadDepartments().forEach(d -> batchDeptFilter.getItems().add(d.getId() + " — " + d.getName()));
        batchDeptFilter.setValue("All Departments");
        batchDeptFilter.setStyle(UIStyles.COMBO);

        VBox batchCheckBox = new VBox(5);
        batchCheckBox.setPadding(new Insets(10));
        batchCheckBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8;");
        
        List<CheckBox> batchChecks = new ArrayList<>();
        List<Batch> allBatches = storage.loadBatches();

        Runnable updateBatchList = () -> {
            batchCheckBox.getChildren().clear();
            batchChecks.clear();
            
            String selectedDept = batchDeptFilter.getValue();
            String deptId = null;
            if (selectedDept != null && !selectedDept.equals("All Departments")) {
                deptId = selectedDept.split(" — ")[0];
            }
            
            for (Batch batch : allBatches) {
                if (deptId != null && !deptId.equals(batch.getDepartmentId())) continue;
                
                CheckBox cb = new CheckBox(batch.getName() + " (" + batch.getDepartmentId() + ") - " + batch.getProgramType());
                cb.setUserData(batch.getId());
                batchChecks.add(cb);
                batchCheckBox.getChildren().add(cb);
            }
            
            if (batchChecks.isEmpty()) {
                Label noBatchesLabel = new Label("No batches found for selected department");
                noBatchesLabel.setStyle("-fx-text-fill: #999; -fx-padding: 10;");
                batchCheckBox.getChildren().add(noBatchesLabel);
            }
        };
        
        updateBatchList.run();
        batchDeptFilter.valueProperty().addListener((obs, old, newVal) -> updateBatchList.run());

        ScrollPane batchScroll = new ScrollPane(batchCheckBox);
        batchScroll.setFitToWidth(true);
        batchScroll.setPrefHeight(180);
        batchScroll.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8;");

        HBox batchButtonBox = new HBox(10);
        Button batchSelectAllBtn = new Button("Select All Batches");
        batchSelectAllBtn.setStyle(UIStyles.BTN_PRIMARY);
        batchSelectAllBtn.setOnAction(e -> batchChecks.forEach(cb -> cb.setSelected(true)));
        
        Button batchClearAllBtn = new Button("Clear All Batches");
        batchClearAllBtn.setStyle(UIStyles.BTN_WARNING);
        batchClearAllBtn.setOnAction(e -> batchChecks.forEach(cb -> cb.setSelected(false)));
        
        batchButtonBox.getChildren().addAll(batchSelectAllBtn, batchClearAllBtn);

        // Course Selection Section
        Label courseSectionLabel = new Label("STEP 2: Select Courses to Assign");
        courseSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");
        courseSectionLabel.setPadding(new Insets(10, 0, 0, 0));

        ComboBox<String> courseDeptFilter = new ComboBox<>();
        courseDeptFilter.getItems().add("All Departments");
        storage.loadDepartments().forEach(d -> courseDeptFilter.getItems().add(d.getId() + " — " + d.getName()));
        courseDeptFilter.setValue("All Departments");
        courseDeptFilter.setStyle(UIStyles.COMBO);

        VBox courseCheckBox = new VBox(5);
        courseCheckBox.setPadding(new Insets(10));
        courseCheckBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8;");
        
        List<CheckBox> courseChecks = new ArrayList<>();
        List<Course> allCourses = storage.loadCourses();

        Runnable updateCourseList = () -> {
            courseCheckBox.getChildren().clear();
            courseChecks.clear();
            
            String selectedDept = courseDeptFilter.getValue();
            String deptId = null;
            if (selectedDept != null && !selectedDept.equals("All Departments")) {
                deptId = selectedDept.split(" — ")[0];
            }
            
            for (Course course : allCourses) {
                if (deptId != null && !deptId.equals(course.getDepartmentId())) continue;
                
                CheckBox cb = new CheckBox(course.getId() + " - " + course.getTitle() + " (" + course.getCredit() + " cr) - " + course.getDepartmentId());
                cb.setUserData(course);
                courseChecks.add(cb);
                courseCheckBox.getChildren().add(cb);
            }
            
            if (courseChecks.isEmpty()) {
                Label noCoursesLabel = new Label("No courses found for selected department");
                noCoursesLabel.setStyle("-fx-text-fill: #999; -fx-padding: 10;");
                courseCheckBox.getChildren().add(noCoursesLabel);
            }
        };
        
        updateCourseList.run();
        courseDeptFilter.valueProperty().addListener((obs, old, newVal) -> updateCourseList.run());

        ScrollPane courseScroll = new ScrollPane(courseCheckBox);
        courseScroll.setFitToWidth(true);
        courseScroll.setPrefHeight(200);
        courseScroll.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8;");

        HBox courseButtonBox = new HBox(10);
        Button courseSelectAllBtn = new Button("Select All Courses");
        courseSelectAllBtn.setStyle(UIStyles.BTN_PRIMARY);
        courseSelectAllBtn.setOnAction(e -> courseChecks.forEach(cb -> cb.setSelected(true)));
        
        Button courseClearAllBtn = new Button("Clear All Courses");
        courseClearAllBtn.setStyle(UIStyles.BTN_WARNING);
        courseClearAllBtn.setOnAction(e -> courseChecks.forEach(cb -> cb.setSelected(false)));
        
        courseButtonBox.getChildren().addAll(courseSelectAllBtn, courseClearAllBtn);

        // Assignment mode
        Label modeLabel = new Label("Assignment Mode:");
        modeLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
        
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton addMode = new RadioButton("Add to existing assignments");
        addMode.setToggleGroup(modeGroup);
        addMode.setSelected(true);
        
        RadioButton replaceMode = new RadioButton("Replace existing assignments");
        replaceMode.setToggleGroup(modeGroup);
        
        HBox modeBox = new HBox(20, addMode, replaceMode);

        content.getChildren().addAll(
            batchSectionLabel, batchDeptFilter, batchScroll, batchButtonBox,
            new Separator(),
            courseSectionLabel, courseDeptFilter, courseScroll, courseButtonBox,
            new Separator(),
            modeLabel, modeBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                List<String> selectedBatchIds = batchChecks.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> cb.getUserData().toString())
                        .collect(Collectors.toList());
                
                List<Course> selectedCourses = courseChecks.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (Course) cb.getUserData())
                        .collect(Collectors.toList());

                if (selectedBatchIds.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please select at least one batch!").showAndWait();
                    return null;
                }
                
                if (selectedCourses.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please select at least one course!").showAndWait();
                    return null;
                }

                boolean isReplaceMode = replaceMode.isSelected();
                int assignedCount = 0;

                for (Course course : selectedCourses) {
                    Set<String> currentBatches = new HashSet<>();
                    if (course.getBatchId() != null && !course.getBatchId().isEmpty() && !isReplaceMode) {
                        String[] ids = course.getBatchId().split(",");
                        for (String id : ids) {
                            currentBatches.add(id.trim());
                        }
                    }
                    
                    // Add new batches
                    currentBatches.addAll(selectedBatchIds);
                    
                    // Update course
                    course.setBatchId(String.join(",", currentBatches));
                    updateCourseInAllBatches(course);
                    assignedCount++;
                }

                // Force a complete refresh of the data
                refresh();
                
                new Alert(Alert.AlertType.INFORMATION, 
                    "✅ " + assignedCount + " courses assigned to " + selectedBatchIds.size() + 
                    " batches successfully!").showAndWait();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void refresh() {
        // Clear the current list
        masterList.clear();
        
        // Load fresh data from storage
        List<Course> allCourses = storage.loadCourses();
        System.out.println("Loading courses: " + allCourses.size() + " courses found");
        
        // Add all courses to the master list
        masterList.addAll(allCourses);
        
        // Update filters
        updateDepartmentFilter();
        
        // Force table refresh
        table.refresh();
        
        // Apply current filters
        applyFilters();
        
        System.out.println("Refresh complete. Displaying " + filteredList.size() + " courses");
    }

    public VBox getRoot() {
        return root;
    }
}