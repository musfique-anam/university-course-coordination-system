package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.util.TimeRules;
import com.scheduler.util.UIStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictManagementView {
    private final DatabaseStorage storage;
    private final VBox root;
    private final TabPane tabPane;
    private ListView<String> classConflictList;
    private ListView<String> examConflictList;
    private Label classSummaryLabel;
    private Label examSummaryLabel;

    public ConflictManagementView(DatabaseStorage storage) {
        this.storage = storage;
        this.tabPane = new TabPane();
        this.root = build();
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

        Label title = new Label("⚠️  Conflict Management");
        title.setStyle(UIStyles.TITLE);

        Button checkAllBtn = new Button("🔍 Check All Conflicts");
        checkAllBtn.setStyle(UIStyles.BTN_PRIMARY);
        checkAllBtn.setOnAction(e -> checkAllConflicts());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        titleBar.getChildren().addAll(title, sp, checkAllBtn);

        // TabPane for Class and Exam conflicts
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        // Class Routine Conflicts Tab
        Tab classTab = new Tab("Class Routine Conflicts");
        classTab.setContent(buildClassConflictTab());
        
        // Exam Routine Conflicts Tab
        Tab examTab = new Tab("Exam Routine Conflicts");
        examTab.setContent(buildExamConflictTab());

        tabPane.getTabs().addAll(classTab, examTab);

        VBox content = new VBox(20, titleBar, tabPane);
        content.setPadding(new Insets(0));
        
        main.getChildren().add(content);
        return main;
    }

    private VBox buildClassConflictTab() {
        VBox tabContent = new VBox(20);
        tabContent.setPadding(new Insets(20));

        // Summary Card
        HBox summaryCard = new HBox(20);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        classSummaryLabel = new Label("No conflicts detected");
        classSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        Button autoFixBtn = new Button("🔄 Auto-Fix Conflicts");
        autoFixBtn.setStyle(UIStyles.BTN_SUCCESS);
        autoFixBtn.setOnAction(e -> autoFixClassConflicts());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(UIStyles.BTN_PRIMARY);
        refreshBtn.setOnAction(e -> checkClassConflicts());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        summaryCard.getChildren().addAll(classSummaryLabel, sp, autoFixBtn, refreshBtn);

        // Conflicts List
        Label listLabel = new Label("Detected Conflicts:");
        listLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");

        classConflictList = new ListView<>();
        classConflictList.setPrefHeight(300);
        classConflictList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.contains("Teacher Conflict") || item.contains("Room Conflict")) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.ORANGE);
                    }
                    setGraphic(new Circle(6, item.contains("Teacher Conflict") ? Color.RED : Color.ORANGE));
                }
            }
        });

        // Manual resolution section
        VBox manualSection = new VBox(10);
        manualSection.setPadding(new Insets(15));
        manualSection.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 12;");

        Label manualLabel = new Label("Manual Resolution");
        manualLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox manualControls = new HBox(10);
        ComboBox<String> conflictTypeCombo = new ComboBox<>();
        conflictTypeCombo.getItems().addAll("Teacher Conflict", "Room Conflict", "Time Conflict");
        conflictTypeCombo.setPromptText("Select conflict type");

        Button resolveBtn = new Button("Resolve Selected");
        resolveBtn.setStyle(UIStyles.BTN_WARNING);
        resolveBtn.setOnAction(e -> resolveManualConflict(conflictTypeCombo.getValue()));

        manualControls.getChildren().addAll(conflictTypeCombo, resolveBtn);
        manualSection.getChildren().addAll(manualLabel, manualControls);

        tabContent.getChildren().addAll(summaryCard, listLabel, classConflictList, manualSection);
        
        // Initial check
        checkClassConflicts();
        
        return tabContent;
    }

    private VBox buildExamConflictTab() {
        VBox tabContent = new VBox(20);
        tabContent.setPadding(new Insets(20));

        // Summary Card
        HBox summaryCard = new HBox(20);
        summaryCard.setPadding(new Insets(20));
        summaryCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        examSummaryLabel = new Label("No conflicts detected");
        examSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        Button autoFixBtn = new Button("🔄 Auto-Fix Conflicts");
        autoFixBtn.setStyle(UIStyles.BTN_SUCCESS);
        autoFixBtn.setOnAction(e -> autoFixExamConflicts());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(UIStyles.BTN_PRIMARY);
        refreshBtn.setOnAction(e -> checkExamConflicts());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        summaryCard.getChildren().addAll(examSummaryLabel, sp, autoFixBtn, refreshBtn);

        // Conflicts List
        Label listLabel = new Label("Detected Conflicts:");
        listLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");

        examConflictList = new ListView<>();
        examConflictList.setPrefHeight(300);
        examConflictList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setTextFill(Color.RED);
                    setGraphic(new Circle(6, Color.RED));
                }
            }
        });

        tabContent.getChildren().addAll(summaryCard, listLabel, examConflictList);
        
        // Initial check
        checkExamConflicts();
        
        return tabContent;
    }

    private void checkAllConflicts() {
        checkClassConflicts();
        checkExamConflicts();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conflict Check Complete");
        alert.setHeaderText("All conflicts have been checked");
        alert.setContentText("Check the respective tabs for details.");
        alert.showAndWait();
    }

    private void checkClassConflicts() {
        List<RoutineEntry> routine = storage.loadRoutine();
        List<String> conflicts = new ArrayList<>();
        
        // Group by day and time slot
        Map<String, List<RoutineEntry>> timeSlotMap = routine.stream()
                .collect(Collectors.groupingBy(r -> r.getDay() + "_" + r.getTimeSlot()));

        // Check for teacher conflicts
        for (Map.Entry<String, List<RoutineEntry>> entry : timeSlotMap.entrySet()) {
            Map<String, List<RoutineEntry>> teacherGroups = entry.getValue().stream()
                    .collect(Collectors.groupingBy(RoutineEntry::getTeacherId));
            
            for (Map.Entry<String, List<RoutineEntry>> teacherEntry : teacherGroups.entrySet()) {
                if (teacherEntry.getValue().size() > 1) {
                    String teacherName = getTeacherName(teacherEntry.getKey());
                    conflicts.add("❌ Teacher Conflict: " + teacherName + " is assigned to " + 
                        teacherEntry.getValue().size() + " classes at " + entry.getKey().replace("_", " "));
                }
            }
        }

        // Check for room conflicts
        for (Map.Entry<String, List<RoutineEntry>> entry : timeSlotMap.entrySet()) {
            Map<String, List<RoutineEntry>> roomGroups = entry.getValue().stream()
                    .collect(Collectors.groupingBy(RoutineEntry::getRoomId));
            
            for (Map.Entry<String, List<RoutineEntry>> roomEntry : roomGroups.entrySet()) {
                if (roomEntry.getValue().size() > 1) {
                    conflicts.add("⚠️ Room Conflict: Room " + roomEntry.getKey() + " has " + 
                        roomEntry.getValue().size() + " classes at " + entry.getKey().replace("_", " "));
                }
            }
        }

        // Check for teacher overload (more than 4 classes per day)
        Map<String, Map<String, Long>> teacherDailyLoad = new HashMap<>();
        for (RoutineEntry r : routine) {
            teacherDailyLoad
                .computeIfAbsent(r.getTeacherId(), k -> new HashMap<>())
                .merge(r.getDay(), 1L, Long::sum);
        }

        for (Map.Entry<String, Map<String, Long>> teacherEntry : teacherDailyLoad.entrySet()) {
            for (Map.Entry<String, Long> dayEntry : teacherEntry.getValue().entrySet()) {
                if (dayEntry.getValue() > 4) {
                    String teacherName = getTeacherName(teacherEntry.getKey());
                    conflicts.add("⚠️ Teacher Overload: " + teacherName + " has " + dayEntry.getValue() + 
                        " classes on " + dayEntry.getKey() + " (max 4)");
                }
            }
        }

        // Update list
        classConflictList.getItems().clear();
        if (conflicts.isEmpty()) {
            classConflictList.getItems().add("✅ No conflicts detected in class routine");
            classSummaryLabel.setText("✅ No conflicts detected");
            classSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            classConflictList.getItems().addAll(conflicts);
            classSummaryLabel.setText("⚠️ " + conflicts.size() + " conflict(s) detected");
            classSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
    }

    private void checkExamConflicts() {
        List<Batch> batches = storage.loadBatches();
        List<String> conflicts = new ArrayList<>();
        
        // Check exam routines for each batch
        for (Batch batch : batches) {
            List<Exam> exams = storage.loadExamRoutine(batch.getId());
            if (exams == null || exams.isEmpty()) continue;

            // Check for duplicate dates
            Set<String> dates = new HashSet<>();
            for (Exam exam : exams) {
                String dateStr = exam.getDate().toString();
                if (dates.contains(dateStr)) {
                    conflicts.add("❌ Exam Conflict: Batch " + batch.getName() + " has multiple exams on " + dateStr);
                }
                dates.add(dateStr);
            }

            // Check for consecutive days (should have gap)
            List<Exam> sortedExams = new ArrayList<>(exams);
            sortedExams.sort(Comparator.comparing(Exam::getDate));
            
            for (int i = 0; i < sortedExams.size() - 1; i++) {
                Exam e1 = sortedExams.get(i);
                Exam e2 = sortedExams.get(i + 1);
                
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(e1.getDate(), e2.getDate());
                if (daysBetween < 1) {
                    conflicts.add("⚠️ Exam Gap Issue: Batch " + batch.getName() + " has exams on " + 
                        e1.getDate() + " and " + e2.getDate() + " with no gap");
                }
            }
        }

        // Update list
        examConflictList.getItems().clear();
        if (conflicts.isEmpty()) {
            examConflictList.getItems().add("✅ No conflicts detected in exam routine");
            examSummaryLabel.setText("✅ No conflicts detected");
            examSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            examConflictList.getItems().addAll(conflicts);
            examSummaryLabel.setText("⚠️ " + conflicts.size() + " conflict(s) detected");
            examSummaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
    }

    private void autoFixClassConflicts() {
        List<RoutineEntry> routine = storage.loadRoutine();
        List<RoutineEntry> fixedRoutine = new ArrayList<>();
        Set<String> usedSlots = new HashSet<>();
        
        // Simple auto-fix: reassign conflicting entries to different time slots
        for (RoutineEntry entry : routine) {
            String key = entry.getDay() + "_" + entry.getTimeSlot() + "_" + entry.getRoomId();
            
            if (usedSlots.contains(key)) {
                // Find a new time slot
                String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};
                String[] slots = TimeRules.TEACHING_SLOTS;
                
                boolean reassigned = false;
                for (String day : days) {
                    for (String slot : slots) {
                        String newKey = day + "_" + slot + "_" + entry.getRoomId();
                        if (!usedSlots.contains(newKey)) {
                            entry.setDay(day);
                            entry.setTimeSlot(slot);
                            usedSlots.add(newKey);
                            reassigned = true;
                            break;
                        }
                    }
                    if (reassigned) break;
                }
            } else {
                usedSlots.add(key);
            }
            fixedRoutine.add(entry);
        }
        
        storage.saveRoutine(fixedRoutine);
        checkClassConflicts();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Auto-Fix Complete");
        alert.setHeaderText("Class Routine Conflicts Fixed");
        alert.setContentText("Conflicting entries have been reassigned to available time slots.");
        alert.showAndWait();
    }

    private void autoFixExamConflicts() {
        List<Batch> batches = storage.loadBatches();
        
        for (Batch batch : batches) {
            List<Exam> exams = storage.loadExamRoutine(batch.getId());
            if (exams == null || exams.isEmpty()) continue;
            
            // Sort by date
            exams.sort(Comparator.comparing(Exam::getDate));
            
            // Ensure at least 1 day gap between exams
            List<Exam> fixedExams = new ArrayList<>();
            Exam prev = null;
            
            for (Exam exam : exams) {
                if (prev != null) {
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(prev.getDate(), exam.getDate());
                    if (daysBetween < 1) {
                        exam.setDate(prev.getDate().plusDays(2));
                    }
                }
                fixedExams.add(exam);
                prev = exam;
            }
            
            storage.saveExamRoutine(batch.getId(), fixedExams);
        }
        
        checkExamConflicts();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Auto-Fix Complete");
        alert.setHeaderText("Exam Routine Conflicts Fixed");
        alert.setContentText("Exam dates have been adjusted to ensure proper gaps.");
        alert.showAndWait();
    }

    private void resolveManualConflict(String conflictType) {
        if (conflictType == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a conflict type to resolve").showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Manual Resolution");
        alert.setHeaderText("Resolving " + conflictType);
        alert.setContentText("This will open a manual resolution dialog. Feature coming soon!");
        alert.showAndWait();
    }

    private String getTeacherName(String teacherId) {
        if (teacherId == null) return "Unknown";
        return storage.loadTeachers().stream()
                .filter(t -> teacherId.equals(t.getTeacherId()))
                .findFirst()
                .map(Teacher::getName)
                .orElse(teacherId);
    }

    public VBox getRoot() {
        return root;
    }
}