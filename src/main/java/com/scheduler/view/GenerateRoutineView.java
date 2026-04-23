package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.service.RoutineGenerator;
import com.scheduler.storage.FileStorage;
import com.scheduler.util.UIStyles;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class GenerateRoutineView {
    private final FileStorage storage;
    private final boolean isExam;
    private final VBox root;
    private VBox deptCheckBoxes;
    private VBox batchCheckBoxes;
    private ComboBox<String> programFilter;
    private TextArea debugArea;
    private Label statusLbl;
    private ProgressIndicator progress;
    private Button generateBtn;

    public GenerateRoutineView(FileStorage storage, boolean isExam) {
        this.storage = storage;
        this.isExam = isExam;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        String titleText = isExam ? "📝  Exam Routine Generator" : "📅  Class Routine Generator";
        Label title = new Label(titleText);
        title.setStyle(UIStyles.TITLE);
        
        Button debugBtn = new Button("🔍 Check Data");
        debugBtn.setStyle(UIStyles.BTN_PRIMARY);
        debugBtn.setOnAction(e -> checkData());

        HBox titleBarRight = new HBox(debugBtn);
        titleBarRight.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(titleBarRight, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, titleBarRight);

        // Data Summary Card
        VBox summaryCard = createSummaryCard();

        // Filter Card
        VBox filterCard = createFilterCard();

        // Generate Button Card
        VBox generateCard = createGenerateCard();

        v.getChildren().addAll(titleBar, summaryCard, filterCard, generateCard);
        return v;
    }

    private VBox createSummaryCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label title = new Label("📊  Data Summary");
        title.setStyle(UIStyles.SUBTITLE);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        
        List<Department> depts = storage.loadDepartments();
        List<Batch> batches = storage.loadBatches();
        List<Teacher> teachers = storage.loadTeachers();
        List<Room> rooms = storage.loadRooms();
        List<Course> courses = storage.loadCourses();
        
        int row = 0;
        grid.add(new Label("Departments:"), 0, row);
        grid.add(new Label(String.valueOf(depts.size())), 1, row++);
        grid.add(new Label("Batches:"), 0, row);
        grid.add(new Label(String.valueOf(batches.size())), 1, row++);
        grid.add(new Label("Teachers:"), 0, row);
        grid.add(new Label(String.valueOf(teachers.size())), 1, row++);
        grid.add(new Label("Rooms:"), 0, row);
        grid.add(new Label(String.valueOf(rooms.size())), 1, row++);
        grid.add(new Label("Courses:"), 0, row);
        grid.add(new Label(String.valueOf(courses.size())), 1, row++);
        
        // Count theory and lab rooms
        long theoryRooms = rooms.stream().filter(r -> r.getRoomType() == RoomType.THEORY).count();
        long labRooms = rooms.stream().filter(r -> r.getRoomType() == RoomType.LAB).count();
        grid.add(new Label("Theory Rooms:"), 0, row);
        grid.add(new Label(String.valueOf(theoryRooms)), 1, row++);
        grid.add(new Label("Lab Rooms:"), 0, row);
        grid.add(new Label(String.valueOf(labRooms)), 1, row++);
        
        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createFilterCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        
        Label title = new Label("🔍  Select Scope");
        title.setStyle(UIStyles.SUBTITLE);

        // Department selection
        Label deptLabel = new Label("Departments:");
        deptLabel.setStyle(UIStyles.LABEL_FORM);
        
        deptCheckBoxes = new VBox(6);
        deptCheckBoxes.setPadding(new Insets(8));
        
        List<Department> depts = storage.loadDepartments();
        if (depts.isEmpty()) {
            deptCheckBoxes.getChildren().add(new Label("No departments found"));
        } else {
            for (Department dep : depts) {
                CheckBox cb = new CheckBox(dep.getId() + " — " + dep.getName());
                cb.setUserData(dep.getId());
                deptCheckBoxes.getChildren().add(cb);
            }
        }
        
        ScrollPane deptScroll = new ScrollPane(deptCheckBoxes);
        deptScroll.setFitToWidth(true);
        deptScroll.setPrefHeight(120);
        deptScroll.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8;");

        // Program filter
        Label progLabel = new Label("Program:");
        progLabel.setStyle(UIStyles.LABEL_FORM);
        
        programFilter = new ComboBox<>(FXCollections.observableArrayList("All", "HSC", "DIPLOMA"));
        programFilter.setValue("All");
        programFilter.setStyle(UIStyles.COMBO);

        // Batch selection
        Label batchLabel = new Label("Batches:");
        batchLabel.setStyle(UIStyles.LABEL_FORM);
        
        batchCheckBoxes = new VBox(6);
        batchCheckBoxes.setPadding(new Insets(8));
        
        List<Batch> batches = storage.loadBatches();
        if (batches.isEmpty()) {
            batchCheckBoxes.getChildren().add(new Label("No batches found"));
        } else {
            for (Batch b : batches) {
                CheckBox cb = new CheckBox(b.getName() + " (" + b.getDepartmentId() + ", " + b.getProgramType() + ")");
                cb.setUserData(b.getId());
                batchCheckBoxes.getChildren().add(cb);
            }
        }
        
        ScrollPane batchScroll = new ScrollPane(batchCheckBoxes);
        batchScroll.setFitToWidth(true);
        batchScroll.setPrefHeight(150);
        batchScroll.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 8;");

        // Clear selection button
        Button clearBtn = new Button("Clear Selection");
        clearBtn.setStyle(UIStyles.BTN_BACK);
        clearBtn.setOnAction(e -> {
            deptCheckBoxes.getChildren().forEach(n -> { 
                if (n instanceof CheckBox) ((CheckBox) n).setSelected(false); 
            });
            batchCheckBoxes.getChildren().forEach(n -> { 
                if (n instanceof CheckBox) ((CheckBox) n).setSelected(false); 
            });
            programFilter.setValue("All");
        });

        HBox progRow = new HBox(12, progLabel, programFilter);
        progRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, deptLabel, deptScroll, progRow, batchLabel, batchScroll, clearBtn);
        return card;
    }

    private VBox createGenerateCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        generateBtn = new Button(isExam ? "🎯  Generate Exam Routine" : "🎯  Generate Class Routine");
        generateBtn.setStyle(UIStyles.BTN_SUCCESS + " -fx-font-size: 15px; -fx-padding: 14 30;");
        generateBtn.setOnAction(e -> generateRoutine());

        progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setMaxSize(30, 30);

        statusLbl = new Label();
        statusLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1565C0;");
        statusLbl.setWrapText(true);

        // Debug area (initially hidden)
        debugArea = new TextArea();
        debugArea.setPrefHeight(150);
        debugArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        debugArea.setVisible(false);
        debugArea.setManaged(false);

        CheckBox showDebugCheck = new CheckBox("Show Debug Info");
        showDebugCheck.setOnAction(e -> {
            boolean show = showDebugCheck.isSelected();
            debugArea.setVisible(show);
            debugArea.setManaged(show);
        });

        HBox btnRow = new HBox(16, generateBtn, progress);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(btnRow, statusLbl, showDebugCheck, debugArea);
        return card;
    }

    // ========== SINGLE getChecked METHOD (KEEP ONLY THIS ONE) ==========
    private Set<String> getChecked(VBox box) {
        Set<String> ids = new HashSet<>();
        for (javafx.scene.Node n : box.getChildren()) {
            if (n instanceof CheckBox && ((CheckBox) n).isSelected() && ((CheckBox) n).getUserData() != null) {
                ids.add(((CheckBox) n).getUserData().toString());
            }
        }
        return ids;
    }

    private void generateRoutine() {
        Set<String> selectedDepts = getChecked(deptCheckBoxes);
        Set<String> selectedBatches = getChecked(batchCheckBoxes);
        String prog = programFilter.getValue();

        if (debugArea != null) {
            debugArea.clear();
            debugArea.appendText("=== GENERATION STARTED ===\n");
            debugArea.appendText("Selected Departments: " + (selectedDepts.isEmpty() ? "All" : selectedDepts) + "\n");
            debugArea.appendText("Selected Batches: " + (selectedBatches.isEmpty() ? "All" : selectedBatches) + "\n");
            debugArea.appendText("Program: " + prog + "\n\n");
        }

        if (!"All".equals(prog)) {
            // Filter batches by program
            final String progFinal = prog;
            List<Batch> matching = storage.loadBatches().stream()
                    .filter(b -> b.getProgramType() != null && b.getProgramType().name().equals(progFinal))
                    .filter(b -> selectedDepts.isEmpty() || selectedDepts.contains(b.getDepartmentId()))
                    .collect(Collectors.toList());
            selectedBatches = matching.stream().map(Batch::getId).collect(Collectors.toSet());
            if (debugArea != null) {
                debugArea.appendText("Filtered by program: " + selectedBatches.size() + " batches\n");
            }
        }

        final Set<String> finalBatches = new HashSet<>(selectedBatches);
        final Set<String> finalDepts = new HashSet<>(selectedDepts);
        
        generateBtn.setDisable(true);
        progress.setVisible(true);
        statusLbl.setText("Generating routine...");

        new Thread(() -> {
            try {
                if (debugArea != null) {
                    debugArea.appendText("Creating RoutineGenerator...\n");
                }
                RoutineGenerator gen = new RoutineGenerator(storage);
                
                if (debugArea != null) {
                    debugArea.appendText("Calling generate()...\n");
                }
                List<RoutineEntry> routine = gen.generate(finalDepts, finalBatches);
                
                Platform.runLater(() -> {
                    if (debugArea != null) {
                        debugArea.appendText("\nGeneration completed. Created " + routine.size() + " entries.\n");
                    }
                    generateBtn.setDisable(false);
                    progress.setVisible(false);
                    
                    if (routine.isEmpty()) {
                        if (debugArea != null) {
                            debugArea.appendText("\n❌ No routine generated! Possible issues:\n");
                            debugArea.appendText("1. No teachers interested in courses\n");
                            debugArea.appendText("2. Teacher availability not set\n");
                            debugArea.appendText("3. No available rooms\n");
                            debugArea.appendText("4. Teacher workload exceeded\n");
                        }
                        
                        statusLbl.setText("⚠ Could not generate conflict-free routine. Check debug info.");
                        
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Generation Failed");
                        alert.setHeaderText("Could not generate conflict-free routine");
                        alert.setContentText("Check the debug area for details.");
                        alert.showAndWait();
                    } else {
                        storage.saveRoutine(routine);
                        if (debugArea != null) {
                            debugArea.appendText("\n✅ Saved " + routine.size() + " routine entries\n");
                        }
                        statusLbl.setText("✅ Routine generated: " + routine.size() + " entries");
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Routine Generated Successfully");
                        alert.setContentText(routine.size() + " entries created.\nGo to View Routine to see them.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (debugArea != null) {
                        debugArea.appendText("\n❌ Error: " + ex.getMessage() + "\n");
                    }
                    ex.printStackTrace();
                    generateBtn.setDisable(false);
                    progress.setVisible(false);
                    statusLbl.setText("❌ Error: " + ex.getMessage());
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Generation failed");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void checkData() {
        StringBuilder report = new StringBuilder();
        report.append("=== DETAILED DATA CHECK REPORT ===\n\n");
        
        List<Teacher> teachers = storage.loadTeachers();
        List<Course> courses = storage.loadCourses();
        List<Room> rooms = storage.loadRooms();
        List<Batch> batches = storage.loadBatches();
        List<Department> depts = storage.loadDepartments();
        
        report.append("DEPARTMENTS: ").append(depts.size()).append("\n");
        for (Department d : depts) {
            report.append("  - ").append(d.getId()).append(": ").append(d.getName()).append("\n");
        }
        
        report.append("\nBATCHES: ").append(batches.size()).append("\n");
        for (Batch b : batches) {
            report.append("  - ").append(b.getName()).append(" (").append(b.getId()).append(")")
                  .append(" Dept: ").append(b.getDepartmentId())
                  .append(" Program: ").append(b.getProgramType())
                  .append(" Courses: ").append(b.getCourses().size()).append("\n");
        }
        
        report.append("\nTEACHERS: ").append(teachers.size()).append("\n");
        for (Teacher t : teachers) {
            report.append("\n  Teacher: ").append(t.getName()).append(" (").append(t.getTeacherId()).append(")\n");
            report.append("    Dept: ").append(t.getDepartmentId()).append("\n");
            report.append("    Max Credit: ").append(t.getMaxCreditLoad()).append("\n");
            report.append("    Program Preference: ").append(t.getProgramPreference()).append("\n");
            report.append("    Available Days: ").append(t.getAvailableDays()).append("\n");
            report.append("    Available Slots: ").append(t.getAvailableTimeSlots()).append("\n");
            report.append("    Interested Courses: ").append(t.getInterestedCourseCodes()).append("\n");
            
            // Validate teacher data
            if (t.getAvailableDays() == null || t.getAvailableDays().isEmpty()) {
                report.append("    ❌ ERROR: No available days set!\n");
            }
            if (t.getAvailableTimeSlots() == null || t.getAvailableTimeSlots().isEmpty()) {
                report.append("    ❌ ERROR: No available time slots set!\n");
            }
            if (t.getInterestedCourseCodes() == null || t.getInterestedCourseCodes().isEmpty()) {
                report.append("    ❌ ERROR: No interested courses set!\n");
            }
        }
        
        report.append("\nROOMS: ").append(rooms.size()).append("\n");
        long theoryRooms = rooms.stream().filter(r -> r.getRoomType() == RoomType.THEORY).count();
        long labRooms = rooms.stream().filter(r -> r.getRoomType() == RoomType.LAB).count();
        report.append("  Theory Rooms: ").append(theoryRooms).append("\n");
        report.append("  Lab Rooms: ").append(labRooms).append("\n");
        
        for (Room r : rooms) {
            report.append("  - ").append(r.getRoomNo()).append(" (").append(r.getRoomType()).append(")");
            if (r.getDepartmentId() != null) {
                report.append(" Dept: ").append(r.getDepartmentId());
            }
            report.append("\n");
        }
        
        report.append("\nCOURSES: ").append(courses.size()).append("\n");
        
        // Check CSE department specifically
        report.append("\n=== CSE DEPARTMENT ANALYSIS ===\n");
        
        List<Teacher> cseTeachers = teachers.stream()
                .filter(t -> "CSE".equals(t.getDepartmentId()))
                .collect(Collectors.toList());
        
        List<Course> cseCourses = courses.stream()
                .filter(c -> "CSE".equals(c.getDepartmentId()))
                .collect(Collectors.toList());
        
        report.append("CSE Teachers: ").append(cseTeachers.size()).append("\n");
        report.append("CSE Courses: ").append(cseCourses.size()).append("\n\n");
        
        report.append("Teacher-Course Mapping:\n");
        for (Course course : cseCourses) {
            List<Teacher> eligible = cseTeachers.stream()
                    .filter(t -> t.getInterestedCourseCodes() != null && 
                                 t.getInterestedCourseCodes().contains(course.getId()))
                    .collect(Collectors.toList());
            
            if (eligible.isEmpty()) {
                report.append("  ❌ ").append(course.getId()).append(": ").append(course.getTitle())
                      .append(" - NO TEACHERS!\n");
            } else {
                report.append("  ✅ ").append(course.getId()).append(": ").append(course.getTitle())
                      .append(" - Teachers: ").append(eligible.stream()
                          .map(Teacher::getName).collect(Collectors.joining(", "))).append("\n");
            }
        }
        
        // Check teacher availability for each day/time slot
        report.append("\nTeacher Availability Matrix:\n");
        String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};
        String[] slots = {"8:30-9:50", "10:00-11:20", "11:30-12:50", "1:30-2:50", "3:00-4:20", "4:30-5:50"};
        
        for (Teacher t : cseTeachers) {
            report.append("\n").append(t.getName()).append(":\n");
            for (String day : days) {
                if (t.getAvailableDays() != null && t.getAvailableDays().contains(day)) {
                    List<String> availableSlots = new ArrayList<>();
                    for (String slot : slots) {
                        if (t.getAvailableTimeSlots() != null && t.getAvailableTimeSlots().contains(slot)) {
                            availableSlots.add(slot);
                        }
                    }
                    report.append("  ").append(day).append(": ").append(availableSlots).append("\n");
                }
            }
        }
        
        // Check rooms for CSE
        report.append("\nCSE Rooms:\n");
        List<Room> cseRooms = rooms.stream()
                .filter(r -> "CSE".equals(r.getDepartmentId()) || r.getDepartmentId() == null)
                .collect(Collectors.toList());
        
        for (Room r : cseRooms) {
            report.append("  - ").append(r.getRoomNo()).append(" (").append(r.getRoomType()).append(")\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Check Report");
        alert.setHeaderText("System Data Status");
        
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(800);
        textArea.setPrefHeight(600);
        
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        alert.getDialogPane().setContent(scrollPane);
        alert.setResizable(true);
        alert.showAndWait();
    }

    public VBox getRoot() { return root; }
}