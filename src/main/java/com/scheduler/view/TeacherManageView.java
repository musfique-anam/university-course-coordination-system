package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.model.*;
import com.scheduler.storage.FileStorage;
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

public class TeacherManageView {
    private final FileStorage storage;
    private final ObservableList<Teacher> masterList = FXCollections.observableArrayList();
    private final FilteredList<Teacher> filteredList;
    private final TableView<Teacher> table = new TableView<>();
    private final VBox root;
    private ComboBox<String> deptFilterCombo;

    public TeacherManageView(FileStorage storage) {
        this.storage = storage;
        this.filteredList = new FilteredList<>(masterList, p -> true);
        this.root = build();
        refresh();
    }

    private VBox build() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(20));
        main.setStyle(UIStyles.BG_MAIN);

        // ================= TITLE BAR =================
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("👨‍🏫  Teacher Management");
        title.setStyle(UIStyles.TITLE);

        // ================= DEPARTMENT FILTER =================
        Label filterLabel = new Label("Filter by Dept:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64;");
        
        deptFilterCombo = new ComboBox<>();
        deptFilterCombo.getItems().add("All Departments");
        deptFilterCombo.setValue("All Departments");
        deptFilterCombo.setStyle(UIStyles.COMBO);
        deptFilterCombo.setPrefWidth(200);
        
        // Update filter when department selection changes
        deptFilterCombo.setOnAction(e -> applyDepartmentFilter());

        Button addBtn = new Button("+ Add Teacher");
        addBtn.setStyle(UIStyles.BTN_SUCCESS);
        addBtn.setOnAction(e -> showAddTeacherDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox filterBox = new HBox(10, filterLabel, deptFilterCombo);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        titleBar.getChildren().addAll(title, spacer, filterBox, addBtn);

        // ================= TABLE =================
        TableColumn<Teacher, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeacherId()));
        
        TableColumn<Teacher, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        
        TableColumn<Teacher, String> shortCol = new TableColumn<>("Short");
        shortCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShortName()));
        
        TableColumn<Teacher, String> deptCol = new TableColumn<>("Dept");
        deptCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDepartmentId() != null ? c.getValue().getDepartmentId() : ""));
        
        TableColumn<Teacher, String> creditCol = new TableColumn<>("Max Credit");
        creditCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getMaxCreditLoad())));
        
        TableColumn<Teacher, String> daysCol = new TableColumn<>("Days");
        daysCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAvailableDays() != null ? String.join(", ", c.getValue().getAvailableDays()) : ""));
        daysCol.setPrefWidth(150);

        TableColumn<Teacher, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(300);
        actCol.setCellFactory(cc -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑 Delete");
            private final Button routineBtn = new Button("📅 Routine");

            {
                editBtn.setStyle(UIStyles.BTN_WARNING + "-fx-font-size: 11px; -fx-padding: 5 10;");
                delBtn.setStyle(UIStyles.BTN_DANGER + "-fx-font-size: 11px; -fx-padding: 5 10;");
                routineBtn.setStyle(UIStyles.BTN_PRIMARY + "-fx-font-size: 11px; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> editTeacher(getTableRow().getItem()));
                delBtn.setOnAction(e -> deleteTeacher(getTableRow().getItem()));
                routineBtn.setOnAction(e -> viewOrDownloadRoutine(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, editBtn, routineBtn, delBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, shortCol, deptCol, creditCol, daysCol, actCol);
        table.setItems(filteredList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(450);

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        main.getChildren().addAll(titleBar, tableCard);
        return main;
    }

    private void applyDepartmentFilter() {
        if (filteredList == null || deptFilterCombo == null) return;
        
        String selectedDept = deptFilterCombo.getValue();
        if (selectedDept == null || selectedDept.equals("All Departments")) {
            filteredList.setPredicate(t -> true);
        } else {
            String deptId = selectedDept.split(" — ")[0];
            filteredList.setPredicate(t -> t != null && deptId.equals(t.getDepartmentId()));
        }
    }

    private void editTeacher(Teacher t) {
        if (t == null) return;
        showTeacherDialog(t, false);
    }

    private void deleteTeacher(Teacher t) {
        if (t == null) return;

        boolean hasRoutine = storage.loadRoutine().stream().anyMatch(r -> t.getTeacherId().equals(r.getTeacherId()));
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        if (hasRoutine) {
            confirm.setTitle("Cannot Delete Teacher");
            confirm.setHeaderText("Teacher has assigned routines!");
            confirm.setContentText("Remove all routines for this teacher before deleting.");
            confirm.showAndWait();
            return;
        } else {
            confirm.setTitle("Delete Teacher");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("Delete teacher: " + t.getName() + "?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    masterList.remove(t);
                    storage.saveTeachers(new ArrayList<>(masterList));
                    updateDepartmentFilter();
                }
            });
        }
    }

    private void showAddTeacherDialog() {
        showTeacherDialog(null, true);
    }

    private void showTeacherDialog(Teacher existing, boolean isNew) {
        Dialog<Teacher> d = new Dialog<>();
        d.setTitle(isNew ? "Add New Teacher" : "Edit Teacher: " + (existing != null ? existing.getName() : ""));
        d.getDialogPane().setPrefSize(700, 720);

        List<Department> depts = storage.loadDepartments();

        // ================= FORM FIELDS =================
        TextField idField = new TextField(existing != null ? existing.getTeacherId() : "");
        idField.setPromptText("e.g. T001");
        idField.setStyle(UIStyles.INPUT);
        if (!isNew) idField.setEditable(false);

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Full name");
        nameField.setStyle(UIStyles.INPUT);

        TextField shortNameField = new TextField(existing != null ? existing.getShortName() : "");
        shortNameField.setPromptText("e.g. MDR");
        shortNameField.setStyle(UIStyles.INPUT);

        TextField emailField = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");
        emailField.setPromptText("email@example.com");
        emailField.setStyle(UIStyles.INPUT);

        TextField phoneField = new TextField(existing != null && existing.getPhone() != null ? existing.getPhone() : "");
        phoneField.setPromptText("01XXXXXXXXX");
        phoneField.setStyle(UIStyles.INPUT);

        ComboBox<String> deptCombo = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).collect(Collectors.toList())));
        deptCombo.setStyle(UIStyles.COMBO);
        deptCombo.setPrefWidth(280);
        
        if (existing != null && existing.getDepartmentId() != null) {
            deptCombo.getItems().stream().filter(s -> s.startsWith(existing.getDepartmentId())).findFirst().ifPresent(deptCombo::setValue);
        } else if (!depts.isEmpty()) deptCombo.getSelectionModel().select(0);

        Spinner<Integer> maxCredit = new Spinner<>(1, 30, existing != null ? existing.getMaxCreditLoad() : 12);
        maxCredit.setEditable(true);
        maxCredit.setPrefWidth(100);

        ComboBox<String> programCombo = new ComboBox<>(FXCollections.observableArrayList("HSC", "DIPLOMA", "BOTH"));
        programCombo.setStyle(UIStyles.COMBO);
        if (existing != null && existing.getProgramPreference() != null) {
            programCombo.setValue(existing.getProgramPreference().name());
        } else programCombo.getSelectionModel().select(0);

        // Courses container
        VBox courseCheckBox = new VBox(5);
        courseCheckBox.setPadding(new Insets(8));
        List<CheckBox> courseChecks = new ArrayList<>();
        
        // Initial course list
        updateCourseList(deptCombo, existing, courseCheckBox, courseChecks);
        
        // Add listener to update courses when department changes
        deptCombo.setOnAction(e -> updateCourseList(deptCombo, existing, courseCheckBox, courseChecks));

        // Days
        String[] allDays = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        FlowPane daysBox = new FlowPane(Orientation.HORIZONTAL, 10, 5);
        daysBox.setPadding(new Insets(8));
        daysBox.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8; -fx-background-color: #FAFAFA; -fx-background-radius: 8;");
        List<CheckBox> dayChecks = new ArrayList<>();
        for (String day : allDays) {
            CheckBox cb = new CheckBox(day.substring(0, 3));
            cb.setUserData(day);
            if (existing != null && existing.getAvailableDays() != null) cb.setSelected(existing.getAvailableDays().contains(day));
            else cb.setSelected(true);
            dayChecks.add(cb);
            daysBox.getChildren().add(cb);
        }

        // Time slots
        FlowPane slotsBox = new FlowPane(Orientation.HORIZONTAL, 10, 5);
        slotsBox.setPadding(new Insets(8));
        slotsBox.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8; -fx-background-color: #FAFAFA; -fx-background-radius: 8;");
        List<CheckBox> slotChecks = new ArrayList<>();
        for (String slot : TimeRules.TEACHING_SLOTS) {
            CheckBox cb = new CheckBox(slot);
            cb.setUserData(slot);
            if (existing != null && existing.getAvailableTimeSlots() != null) cb.setSelected(existing.getAvailableTimeSlots().contains(slot));
            else cb.setSelected(true);
            slotChecks.add(cb);
            slotsBox.getChildren().add(cb);
        }

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(isNew ? "Set login password" : "Leave blank to keep current");
        passwordField.setStyle(UIStyles.INPUT);

        // ================= FORM LAYOUT =================
        GridPane form = new GridPane();
        form.setHgap(16); form.setVgap(14); form.setPadding(new Insets(16));
        int row = 0;
        form.add(lbl("Teacher ID *"), 0, row); form.add(idField, 1, row++);
        form.add(lbl("Full Name *"), 0, row); form.add(nameField, 1, row++);
        form.add(lbl("Short Name"), 0, row); form.add(shortNameField, 1, row++);
        form.add(lbl("Email"), 0, row); form.add(emailField, 1, row++);
        form.add(lbl("Phone"), 0, row); form.add(phoneField, 1, row++);
        form.add(lbl("Department *"), 0, row); form.add(deptCombo, 1, row++);
        form.add(lbl("Max Credit Load"), 0, row); form.add(maxCredit, 1, row++);
        form.add(lbl("Program Preference"), 0, row); form.add(programCombo, 1, row++);
        form.add(lbl("Interested Courses"), 0, row); 
        
        ScrollPane courseScroll = new ScrollPane(courseCheckBox);
        courseScroll.setFitToWidth(true);
        courseScroll.setPrefHeight(120);
        courseScroll.setStyle("-fx-border-color: #CFD8DC; -fx-border-radius: 8;");
        form.add(courseScroll, 1, row++);
        
        form.add(lbl("Available Days"), 0, row); form.add(daysBox, 1, row++);
        form.add(lbl("Available Time Slots"), 0, row); form.add(slotsBox, 1, row++);
        form.add(lbl("Login Password"), 0, row); form.add(passwordField, 1, row++);

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setPrefHeight(600);
        d.getDialogPane().setContent(formScroll);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ================= SAVE ACTION =================
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            if (id.isEmpty() || name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "ID and Name required!").showAndWait();
                return null;
            }
            if (isNew && masterList.stream().anyMatch(t -> id.equals(t.getTeacherId()))) {
                new Alert(Alert.AlertType.WARNING, "Teacher ID already exists!").showAndWait();
                return null;
            }

            Teacher t = existing != null ? existing : new Teacher();
            t.setTeacherId(id);
            t.setName(name);
            t.setShortName(shortNameField.getText().trim());
            t.setEmail(emailField.getText());
            t.setPhone(phoneField.getText());
            String deptSel = deptCombo.getValue();
            t.setDepartmentId(deptSel != null ? deptSel.split(" — ")[0] : "");
            t.setMaxCreditLoad(maxCredit.getValue());

            // ProgramType handling
            String progVal = programCombo.getValue();
            if ("BOTH".equals(progVal)) t.setProgramPreference(ProgramType.BOTH);
            else if ("HSC".equals(progVal)) t.setProgramPreference(ProgramType.HSC);
            else t.setProgramPreference(ProgramType.DIPLOMA);

            List<String> selectedCourses = courseChecks.stream().filter(CheckBox::isSelected)
                    .map(cb -> cb.getText().split(" — ")[0]).collect(Collectors.toList());
            t.setInterestedCourseCodes(selectedCourses);

            List<String> selectedDays = dayChecks.stream().filter(CheckBox::isSelected)
                    .map(cb -> cb.getUserData().toString()).collect(Collectors.toList());
            t.setAvailableDays(selectedDays);

            List<String> selectedSlots = slotChecks.stream().filter(CheckBox::isSelected)
                    .map(cb -> cb.getUserData().toString()).collect(Collectors.toList());
            t.setAvailableTimeSlots(selectedSlots.isEmpty() ? Arrays.asList(TimeRules.TEACHING_SLOTS) : selectedSlots);

            String pass = passwordField.getText();
            if (!pass.isEmpty()) t.setPasswordHash(AuthService.simpleHash(pass));
            else if (isNew) {
                new Alert(Alert.AlertType.WARNING, "Password required for new teacher!").showAndWait();
                return null;
            }

            return t;
        });

        d.showAndWait().ifPresent(t -> {
            if (t != null) {
                if (isNew) masterList.add(t);
                storage.saveTeachers(new ArrayList<>(masterList));
                table.refresh();
                updateDepartmentFilter();
            }
        });
    }

    private void updateCourseList(ComboBox<String> deptCombo, Teacher existing, VBox courseBox, List<CheckBox> courseChecks) {
        courseBox.getChildren().clear();
        courseChecks.clear();
        
        String selectedDept = deptCombo.getValue();
        if (selectedDept == null || selectedDept.equals("All Departments")) {
            // Show all courses
            Set<String> allCourseCodes = new HashSet<>();
            for (Batch b : storage.loadBatches()) {
                for (Course c : b.getCourses()) {
                    allCourseCodes.add(c.getId() + " — " + c.getTitle());
                }
            }
            for (String cs : allCourseCodes.stream().sorted().collect(Collectors.toList())) {
                CheckBox cb = new CheckBox(cs);
                if (existing != null && existing.getInterestedCourseCodes() != null) {
                    String code = cs.contains(" — ") ? cs.split(" — ")[0] : cs;
                    cb.setSelected(existing.getInterestedCourseCodes().contains(code));
                }
                courseChecks.add(cb);
                courseBox.getChildren().add(cb);
            }
        } else {
            // Show only courses from selected department
            String deptId = selectedDept.split(" — ")[0];
            Set<String> deptCourses = new HashSet<>();
            for (Batch b : storage.loadBatches()) {
                if (deptId.equals(b.getDepartmentId())) {
                    for (Course c : b.getCourses()) {
                        deptCourses.add(c.getId() + " — " + c.getTitle());
                    }
                }
            }
            for (String cs : deptCourses.stream().sorted().collect(Collectors.toList())) {
                CheckBox cb = new CheckBox(cs);
                if (existing != null && existing.getInterestedCourseCodes() != null) {
                    String code = cs.contains(" — ") ? cs.split(" — ")[0] : cs;
                    cb.setSelected(existing.getInterestedCourseCodes().contains(code));
                }
                courseChecks.add(cb);
                courseBox.getChildren().add(cb);
            }
        }
    }

    private void viewOrDownloadRoutine(Teacher t) {
        if (t == null) return;

        List<RoutineEntry> routines = storage.loadRoutine().stream()
                .filter(r -> t.getTeacherId().equals(r.getTeacherId()))
                .collect(Collectors.toList());

        if (routines.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No routines assigned for this teacher.").showAndWait();
            return;
        }

        Dialog<Void> d = new Dialog<>();
        d.setTitle("Routine: " + t.getName());

        TableView<RoutineEntry> routineTable = new TableView<>();
        routineTable.setPrefHeight(300);

        TableColumn<RoutineEntry, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDay()));

        TableColumn<RoutineEntry, String> slotCol = new TableColumn<>("Time");
        slotCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTimeSlot()));

        TableColumn<RoutineEntry, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getCourseCode() != null ? c.getValue().getCourseCode() : "") +
                        " " + (c.getValue().getCourseTitle() != null ? c.getValue().getCourseTitle() : "")
        ));

        TableColumn<RoutineEntry, String> batchCol = new TableColumn<>("Batch(es)");
        batchCol.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getBatchNames())));

        TableColumn<RoutineEntry, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomNo() != null ? c.getValue().getRoomNo() : "—"));

        routineTable.getColumns().addAll(dayCol, slotCol, courseCol, batchCol, roomCol);
        routineTable.setItems(FXCollections.observableArrayList(routines));

        Button downloadBtn = new Button("Download Routine");
        downloadBtn.setStyle(UIStyles.BTN_PRIMARY);
        downloadBtn.setOnAction(e -> {
            boolean success = storage.exportTeacherRoutine(t.getTeacherId());
            new Alert(Alert.AlertType.INFORMATION, success ? "Routine downloaded successfully." : "Failed to download routine.").showAndWait();
        });

        VBox content = new VBox(12, routineTable, downloadBtn);
        content.setPadding(new Insets(10));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle(UIStyles.LABEL_FORM);
        return l;
    }

    private void refresh() {
        masterList.clear();
        masterList.addAll(storage.loadTeachers());
        updateDepartmentFilter();
    }
    
    private void updateDepartmentFilter() {
        if (deptFilterCombo == null) return;
        
        Set<String> depts = masterList.stream()
                .map(t -> t.getDepartmentId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        String currentSelection = deptFilterCombo.getValue();
        deptFilterCombo.getItems().clear();
        deptFilterCombo.getItems().add("All Departments");
        
        List<Department> allDepts = storage.loadDepartments();
        for (String deptId : depts) {
            allDepts.stream()
                    .filter(d -> deptId.equals(d.getId()))
                    .findFirst()
                    .ifPresent(d -> deptFilterCombo.getItems().add(d.getId() + " — " + d.getName()));
        }
        
        if (currentSelection != null && deptFilterCombo.getItems().contains(currentSelection)) {
            deptFilterCombo.setValue(currentSelection);
        } else {
            deptFilterCombo.setValue("All Departments");
        }
        applyDepartmentFilter();
    }

    public VBox getRoot() {
        return root;
    }
}