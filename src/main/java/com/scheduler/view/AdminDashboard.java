package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.model.*;
import com.scheduler.storage.DatabaseStorage; // <-- MUST BE DatabaseStorage
import com.scheduler.util.UIStyles;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboard {

    private final DatabaseStorage storage; // <-- MUST BE DatabaseStorage
    private final AuthService authService;
    private final Stage stage;
    private final BorderPane root;
    private StackPane contentPane;
    private Button activeMenuBtn = null;

    // <-- MUST BE DatabaseStorage
    public AdminDashboard(DatabaseStorage storage, AuthService authService, Stage stage) {
        this.storage = storage;
        this.authService = authService;
        this.stage = stage;
        this.root = new BorderPane();
        this.contentPane = new StackPane();
        build();
    }

    private void build() {
        root.setStyle(UIStyles.BG_MAIN);

        // Header
        HBox header = buildHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        contentPane.setPadding(new Insets(24.0));
        contentPane.setStyle("-fx-background-color: #F0F4F8;");
        root.setCenter(contentPane);

        showScreen("Home");
    }

    private HBox buildHeader() {
        HBox h = new HBox();
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(0.0, 24.0, 0.0, 0.0));
        h.setStyle("-fx-background-color: linear-gradient(to right, #0D2137, #1565C0); -fx-pref-height: 60;");

        // Logo area
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0.0, 20.0, 0.0, 0.0));
        logoBox.setPrefWidth(220);
        logoBox.setStyle("-fx-background-color: rgba(0,0,0,0.2);");

        Label logoText = new Label("SAS");
        logoText.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        Label logoSub = new Label("Scheduler");
        logoSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.6);");
        VBox logoVBox = new VBox(0, logoText, logoSub);
        logoBox.getChildren().add(logoVBox);

        Label title = new Label("Smart Academic Scheduler — Admin Panel");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 Administrator");
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> doLogout());

        h.getChildren().addAll(logoBox, title, spacer, userLabel, new Label("   "), logoutBtn);
        return h;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(2);
        sidebar.setPadding(new Insets(16.0, 8.0, 16.0, 8.0));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #0D2137;");

        String[][] menuItems = {
            {"🏠", "Home", "Home"},
            {"🏛", "Departments", "Departments"},
            {"📚", "Batches", "Batches"},
            {"📖", "Course Management", "CourseManagement"},
            {"👨‍🏫", "Teachers", "Teachers"},
            {"🚪", "Rooms", "Rooms"},
            {"🏗", "Room Management", "Room Management"},
            {"🔀", "Merged Classes", "Merged Classes"},
            {"📅", "Class Routine", "Class Routine"},
            {"📝", "Exam Routine", "Exam Routine"},
            {"👁", "View Routine", "View Routine"},
            {"⚠️", "Conflict Management", "ConflictManagement"}
        };

        for (String[] item : menuItems) {
            Button btn = createMenuButton(item[0], item[1], item[2]);
            sidebar.getChildren().add(btn);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Label version = new Label("v2.0 — Smart Scheduler");
        version.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.3); -fx-padding: 8 16;");
        sidebar.getChildren().add(version);

        return sidebar;
    }

    private Button createMenuButton(String icon, String label, String screen) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(UIStyles.BTN_MENU);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            showScreen(screen);
            setActiveButton(btn);
        });
        btn.setOnMouseEntered(e -> {
            if (btn != activeMenuBtn) btn.setStyle(UIStyles.BTN_MENU + UIStyles.BTN_MENU_HOVER);
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeMenuBtn) btn.setStyle(UIStyles.BTN_MENU);
        });
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (activeMenuBtn != null) activeMenuBtn.setStyle(UIStyles.BTN_MENU);
        activeMenuBtn = btn;
        btn.setStyle(UIStyles.BTN_MENU_ACTIVE);
    }

    private void showScreen(String name) {
        Node view;
        switch (name) {
            case "Home":
                view = buildHomeView();
                break;
            case "Departments":
                view = wrap(new DepartmentManageView(storage).getRoot());
                break;
            case "Batches":
                view = wrap(new BatchManageView(storage).getRoot());
                break;
            case "CourseManagement":
                view = wrap(new CourseManagementView(storage).getRoot());
                break;
            case "Teachers":
                view = wrap(new TeacherManageView(storage).getRoot());
                break;
            case "Rooms":
                view = wrap(new RoomManageView(storage).getRoot());
                break;
            case "Room Management":
                view = wrap(new RoomAssignmentView(storage).getRoot());
                break;
            case "Merged Classes":
                view = wrap(new MergeClassView(storage).getRoot());
                break;
            case "Class Routine":
                view = wrap(new GenerateRoutineView(storage, false).getRoot());
                break;
            case "Exam Routine":
                view = wrap(new GenerateRoutineView(storage, true).getRoot());
                break;
            case "View Routine":
                view = wrap(new RoutineView(storage).getRoot());
                break;
            case "ConflictManagement":
                view = wrap(new ConflictManagementView(storage).getRoot());
                break;
            default:
                view = new Label("Select a menu");
        }
        // Animate transition
        FadeTransition fade = new FadeTransition(Duration.millis(250), view);
        fade.setFromValue(0); 
        fade.setToValue(1);
        contentPane.getChildren().setAll(view);
        fade.play();
    }

    private Node buildHomeView() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox v = new VBox(24);
        v.setPadding(new Insets(8.0));

        // Welcome banner
        HBox banner = buildBanner();
        
        // Stats grid
        GridPane statsGrid = buildStatsGrid();
        
        // Quick actions
        Node quickActions = buildQuickActions();
        
        // Charts section
        Node chartsSection = buildChartsSection();
        
        // Batch overview
        Node batchSection = buildBatchOverview();
        
        // Recent activity
        Node recentActivity = buildRecentActivity();

        v.getChildren().addAll(banner, statsGrid, quickActions, chartsSection, batchSection, recentActivity);
        scroll.setContent(v);
        return scroll;
    }

    private HBox buildBanner() {
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(24.0, 30.0, 24.0, 30.0));
        banner.setStyle("-fx-background-color: linear-gradient(to right, #1565C0, #0097A7); -fx-background-radius: 14;");
        
        VBox bannerText = new VBox(6);
        Label welcome = new Label("Welcome to Smart Academic Scheduler");
        welcome.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label welcomeSub = new Label("Manage departments, batches, teachers, rooms, courses, and resolve conflicts.");
        welcomeSub.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.85);");
        bannerText.getChildren().addAll(welcome, welcomeSub);
        banner.getChildren().add(bannerText);
        
        return banner;
    }

    private GridPane buildStatsGrid() {
        List<Department> depts = storage.loadDepartments();
        List<Batch> batches = storage.loadBatches();
        List<Teacher> teachers = storage.loadTeachers();
        List<Room> rooms = storage.loadRooms();
        List<Course> courses = storage.loadCourses();
        List<RoutineEntry> routine = storage.loadRoutine();

        long theoryRooms = rooms.stream().filter(r -> r.getRoomType() != null && r.getRoomType().name().equals("THEORY")).count();
        long labRooms = rooms.stream().filter(r -> r.getRoomType() != null && r.getRoomType().name().equals("LAB")).count();
        long hscBatches = batches.stream().filter(b -> b.getProgramType() == ProgramType.HSC).count();
        long dipBatches = batches.stream().filter(b -> b.getProgramType() == ProgramType.DIPLOMA).count();

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);
        
        // Create column constraints
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            statsGrid.getColumnConstraints().add(col);
        }

        statsGrid.add(statCard("🏛", "Departments", String.valueOf(depts.size()), "#1565C0"), 0, 0);
        statsGrid.add(statCard("📚", "Total Batches", String.valueOf(batches.size()), "#2E7D32"), 1, 0);
        statsGrid.add(statCard("📖", "Total Courses", String.valueOf(courses.size()), "#9C27B0"), 2, 0);
        statsGrid.add(statCard("👨‍🏫", "Teachers", String.valueOf(teachers.size()), "#6A1B9A"), 3, 0);
        statsGrid.add(statCard("📖", "Theory Rooms", String.valueOf(theoryRooms), "#E65100"), 0, 1);
        statsGrid.add(statCard("🖥", "Lab Rooms", String.valueOf(labRooms), "#AD1457"), 1, 1);
        statsGrid.add(statCard("📅", "Routine Entries", String.valueOf(routine.size()), "#1B5E20"), 2, 1);
        statsGrid.add(statCard("🎓", "HSC / Diploma", hscBatches + " / " + dipBatches, "#37474F"), 3, 1);

        return statsGrid;
    }

    private VBox statCard(String icon, String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20.0));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(UIStyles.STAT_CARD);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 28px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #78909C;");

        // Bottom accent bar
        Rectangle bar = new Rectangle(40, 4);
        bar.setArcWidth(4); 
        bar.setArcHeight(4);
        bar.setFill(Color.web(color));

        card.getChildren().addAll(iconLbl, valueLbl, nameLbl, bar);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(UIStyles.STAT_CARD + "-fx-effect: dropshadow(gaussian, " + color + "44, 16, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle(UIStyles.STAT_CARD));

        return card;
    }

    private Node buildQuickActions() {
        VBox actionSection = new VBox(16);
        actionSection.setPadding(new Insets(20));
        actionSection.setStyle(UIStyles.BG_CARD);
        
        Label actionTitle = new Label("⚡  Quick Actions");
        actionTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");
        
        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(16);
        actionGrid.setVgap(16);
        
        String[][] actions = {
            {"📥", "Load from JSON", "#4CAF50"},
            {"📅", "Generate Routine", "#2196F3"},
            {"📖", "Course Management", "#9C27B0"},
            {"🔗", "Merge Classes", "#FF9800"},
            {"⚠️", "Check Conflicts", "#E91E63"},
            {"📧", "Send Notifications", "#607D8B"}
        };
        
        for (int i = 0; i < actions.length; i++) {
            Button actionBtn = createQuickActionBtn(actions[i][0], actions[i][1], actions[i][2]);
            int index = i;
            actionBtn.setOnAction(e -> handleQuickAction(actions[index][1]));
            actionGrid.add(actionBtn, i % 3, i / 3);
        }
        
        actionSection.getChildren().addAll(actionTitle, actionGrid);
        return actionSection;
    }

    private Button createQuickActionBtn(String icon, String text, String color) {
        Button btn = new Button(icon + "  " + text);
        btn.setStyle(
            "-fx-background-color: white; " +
            "-fx-text-fill: #37474F; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 12 20; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #E0E0E0; " +
            "-fx-border-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );
        btn.setPrefWidth(180);
        
        // Hover effect
        btn.setOnMouseEntered(e -> 
            btn.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 12 20; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, " + color + "44, 10, 0, 0, 4);"
            )
        );
        
        btn.setOnMouseExited(e -> 
            btn.setStyle(
                "-fx-background-color: white; " +
                "-fx-text-fill: #37474F; " +
                "-fx-font-size: 13px; " +
                "-fx-padding: 12 20; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);"
            )
        );
        
        return btn;
    }

    private void handleQuickAction(String action) {
        switch (action) {
            case "Load from JSON":
                loadFromJson();
                break;
            case "Generate Routine":
                showScreen("Class Routine");
                break;
            case "Course Management":
                showScreen("CourseManagement");
                break;
            case "Merge Classes":
                showScreen("Merged Classes");
                break;
            case "Check Conflicts":
                showScreen("ConflictManagement");
                break;
            case "Send Notifications":
                showNotificationDialog();
                break;
        }
    }

    // ========== SIMPLIFIED JSON LOADING IMPLEMENTATION ==========
    private void loadFromJson() {
        // Let user choose the file
        File selectedFile = storage.chooseJsonFile(stage);
        
        if (selectedFile != null) {
            try {
                System.out.println("📂 Attempting to load: " + selectedFile.getAbsolutePath());
                boolean success = storage.loadFromJson(selectedFile);
                
                if (success) {
                    // Refresh the home view to show updated stats
                    showScreen("Home");
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Data Loaded Successfully");
                    alert.setContentText("Data loaded from: " + selectedFile.getName() + 
                        "\n\nAll data has been updated. The dashboard has been refreshed.");
                    alert.showAndWait();
                } else {
                    showErrorAlert("Failed to load data from the selected file. Check console for details.");
                }
            } catch (Exception ex) {
                System.err.println("❌ Exception loading data: " + ex.getMessage());
                ex.printStackTrace();
                showErrorAlert("Error loading data: " + ex.getMessage());
            }
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showReportDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reports");
        alert.setHeaderText("Reports Dashboard");
        alert.setContentText("Detailed reports and analytics will be available soon.");
        alert.showAndWait();
    }

    private void showNotificationDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Send Notifications");
        alert.setContentText("Email and SMS notification system coming soon.");
        alert.showAndWait();
    }

    private Node buildChartsSection() {
        VBox chartSection = new VBox(16);
        chartSection.setPadding(new Insets(20));
        chartSection.setStyle(UIStyles.BG_CARD);
        
        Label chartTitle = new Label("📈  Resource Utilization");
        chartTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");
        
        VBox bars = new VBox(12);
        
        // Calculate some sample percentages
        List<Room> rooms = storage.loadRooms();
        List<Teacher> teachers = storage.loadTeachers();
        List<Batch> batches = storage.loadBatches();
        
        int teacherUtilization = teachers.isEmpty() ? 0 : Math.min(100, (int)((teachers.size() * 100) / 20));
        int roomUtilization = rooms.isEmpty() ? 0 : Math.min(100, (int)((rooms.size() * 100) / 15));
        int batchUtilization = batches.isEmpty() ? 0 : Math.min(100, (int)((batches.size() * 100) / 10));
        
        bars.getChildren().add(createProgressBar("Teacher Allocation", teacherUtilization, "#4CAF50"));
        bars.getChildren().add(createProgressBar("Room Usage", roomUtilization, "#2196F3"));
        bars.getChildren().add(createProgressBar("Batch Completion", batchUtilization, "#FF9800"));
        
        chartSection.getChildren().addAll(chartTitle, bars);
        return chartSection;
    }

    private Node createProgressBar(String label, int percentage, String color) {
        HBox barContainer = new HBox(10);
        barContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(label);
        nameLabel.setPrefWidth(150);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #37474F;");
        
        StackPane barBg = new StackPane();
        barBg.setPrefWidth(300);
        barBg.setPrefHeight(20);
        barBg.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 10;");
        
        Region barFill = new Region();
        barFill.setPrefWidth(percentage * 3);
        barFill.setPrefHeight(20);
        barFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10;");
        barFill.setMaxWidth(300);
        
        barBg.getChildren().add(barFill);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        
        Label percentLabel = new Label(percentage + "%");
        percentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        barContainer.getChildren().addAll(nameLabel, barBg, percentLabel);
        return barContainer;
    }

    private Node buildBatchOverview() {
        List<Department> depts = storage.loadDepartments();
        List<Batch> batches = storage.loadBatches();

        VBox batchSection = new VBox(12);
        batchSection.setPadding(new Insets(20.0));
        batchSection.setStyle(UIStyles.BG_CARD);

        Label batchTitle = new Label("📊  Batch Overview by Department");
        batchTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");

        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All Departments");
        if (depts != null) {
            depts.forEach(d -> deptFilter.getItems().add(d.getId() + " - " + d.getName()));
        }
        deptFilter.setValue("All Departments");
        deptFilter.setStyle(UIStyles.COMBO);

        TableView<Batch> batchTable = new TableView<>();
        batchTable.setStyle(UIStyles.TABLE);
        batchTable.setPrefHeight(250);
        batchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Batch, String> nameCol = new TableColumn<>("Batch Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        
        TableColumn<Batch, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDepartmentId()));
        
        TableColumn<Batch, String> progCol = new TableColumn<>("Program");
        progCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getProgramType() != null ? c.getValue().getProgramType().name() : ""));
        
        TableColumn<Batch, Number> studCol = new TableColumn<>("Students");
        studCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTotalStudents()));
        
        TableColumn<Batch, Number> coursesCol = new TableColumn<>("Courses");
        coursesCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCourses().size()));
        
        batchTable.getColumns().addAll(nameCol, deptCol, progCol, studCol, coursesCol);

        javafx.collections.ObservableList<Batch> batchObs = javafx.collections.FXCollections.observableArrayList();
        if (batches != null) {
            batchObs.addAll(batches);
        }
        javafx.collections.transformation.FilteredList<Batch> filteredBatches = 
            new javafx.collections.transformation.FilteredList<>(batchObs, b -> true);
        batchTable.setItems(filteredBatches);

        deptFilter.setOnAction(e -> {
            String sel = deptFilter.getValue();
            if (sel == null || sel.equals("All Departments")) {
                filteredBatches.setPredicate(b -> true);
            } else {
                String deptId = sel.split(" - ")[0];
                filteredBatches.setPredicate(b -> deptId.equals(b.getDepartmentId()));
            }
        });

        HBox filterRow = new HBox(12, new Label("Filter by Department:"), deptFilter);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        batchSection.getChildren().addAll(batchTitle, filterRow, batchTable);
        return batchSection;
    }

    private Node buildRecentActivity() {
        VBox activitySection = new VBox(12);
        activitySection.setPadding(new Insets(20));
        activitySection.setStyle(UIStyles.BG_CARD);
        
        Label activityTitle = new Label("🕒  Recent Activity");
        activityTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");
        
        VBox activityList = new VBox(8);
        
        String[][] activities = {
            {"✅", "System initialized", "Just now", "#4CAF50"},
            {"📝", "Course Management added", "Now", "#2196F3"},
            {"⚠️", "Conflict Management available", "Now", "#FF9800"},
            {"📖", "View and assign courses", "Feature", "#9C27B0"},
            {"🔍", "Check for scheduling conflicts", "Feature", "#E91E63"}
        };
        
        for (String[] act : activities) {
            activityList.getChildren().add(createActivityItem(act[0], act[1], act[2], act[3]));
        }
        
        Button viewAllBtn = new Button("Refresh Dashboard");
        viewAllBtn.setStyle(
            "-fx-background-color: #1565C0; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;"
        );
        viewAllBtn.setMaxWidth(200);
        viewAllBtn.setOnAction(e -> showScreen("Home"));
        
        activitySection.getChildren().addAll(activityTitle, activityList, viewAllBtn);
        return activitySection;
    }

    private HBox createActivityItem(String icon, String text, String time, String color) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8;");
        
        Circle dot = new Circle(6, Color.web(color));
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #37474F;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #78909C;");
        
        item.getChildren().addAll(dot, iconLabel, textLabel, spacer, timeLabel);
        
        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8;"));
        
        return item;
    }

    private Node wrap(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    private void doLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            authService.logout();
            LoginView login = new LoginView(storage, authService, stage);
            stage.setScene(new Scene(login.getRoot(), 900, 600));
            stage.setTitle("Smart Academic Scheduler - Login");
            stage.centerOnScreen();
        }
    }

    private void verifyData() {
    System.out.println("\n=== VERIFYING DATA ===\n");
    
    List<Department> depts = storage.loadDepartments();
    System.out.println("Departments: " + depts.size());
    
    List<Teacher> teachers = storage.loadTeachers();
    System.out.println("\nTeachers: " + teachers.size());
    for (Teacher t : teachers) {
        System.out.println("  " + t.getName() + " (" + t.getTeacherId() + ")");
        System.out.println("    Dept: " + t.getDepartmentId());
        System.out.println("    Interested courses: " + t.getInterestedCourseCodes());
        System.out.println("    Available days: " + t.getAvailableDays());
        System.out.println("    Available slots: " + t.getAvailableTimeSlots());
    }
    
    List<Batch> batches = storage.loadBatches();
    System.out.println("\nBatches: " + batches.size());
    
    List<Course> courses = storage.loadCourses();
    System.out.println("\nCourses: " + courses.size());
    
    // Check teacher-course mapping for CSE department
    System.out.println("\nChecking CSE teacher-course mapping:");
    List<Teacher> cseTeachers = teachers.stream()
            .filter(t -> "CSE".equals(t.getDepartmentId()))
            .collect(Collectors.toList());
    
    List<Course> cseCourses = courses.stream()
            .filter(c -> "CSE".equals(c.getDepartmentId()))
            .collect(Collectors.toList());
    
    for (Course c : cseCourses) {
        boolean hasTeacher = cseTeachers.stream()
                .anyMatch(t -> t.getInterestedCourseCodes() != null && 
                              t.getInterestedCourseCodes().contains(c.getId()));
        System.out.println("  Course " + c.getId() + ": " + (hasTeacher ? "✅" : "❌"));
        
        if (!hasTeacher) {
            System.out.println("    No teacher interested in this course!");
        }
    }
    
    System.out.println("\n=== VERIFICATION COMPLETE ===\n");
}

    public BorderPane getRoot() { 
        return root; 
    }
}