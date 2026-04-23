package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.model.*;
import com.scheduler.storage.FileStorage;
import com.scheduler.util.UIStyles;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class TeacherDashboard {
    private final FileStorage storage;
    private final AuthService authService;
    private final Stage stage;
    private final BorderPane root;
    private final StackPane contentPane;
    private Button activeBtn;

    public TeacherDashboard(FileStorage storage, AuthService authService, Stage stage) {
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
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0.0, 24.0, 0.0, 0.0));
        header.setStyle("-fx-background-color: linear-gradient(to right, #0D2137, #1565C0); -fx-pref-height: 60;");

        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0.0, 20.0, 0.0, 0.0));
        logoBox.setPrefWidth(220);
        logoBox.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        Label logoText = new Label("SAS");
        logoText.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00BCD4;");
        logoBox.getChildren().add(logoText);

        Label title = new Label("Teacher Portal");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Teacher name
        String tid = authService.getCurrentUserId();
        Optional<Teacher> tOpt = storage.loadTeachers().stream().filter(t -> tid.equals(t.getTeacherId())).findFirst();
        Label userLabel = new Label("👨‍🏫 " + tOpt.map(Teacher::getName).orElse(tid));
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            LoginView login = new LoginView(storage, authService, stage);
            stage.setScene(new Scene(login.getRoot(), 900, 600));
            stage.setResizable(false);
            stage.setMaximized(false);
        });
        header.getChildren().addAll(logoBox, title, spacer, userLabel, new Label("  "), logoutBtn);
        root.setTop(header);

        // Sidebar
        VBox sidebar = new VBox(2);
        sidebar.setPadding(new Insets(16.0, 8.0, 16.0, 8.0));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #0D2137;");

        String[][] menus = {{"🏠", "Home", "home"}, {"👤", "My Profile", "profile"}, {"📅", "My Routine", "routine"}, {"⏰", "Availability", "availability"}};
        for (String[] m : menus) {
            Button btn = new Button(m[0] + "  " + m[1]);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle(UIStyles.BTN_MENU);
            btn.setAlignment(Pos.CENTER_LEFT);
            final String screen = m[2];
            btn.setOnAction(e -> { showScreen(screen); setActive(btn); });
            btn.setOnMouseEntered(e -> { if (btn != activeBtn) btn.setStyle(UIStyles.BTN_MENU + UIStyles.BTN_MENU_HOVER); });
            btn.setOnMouseExited(e -> { if (btn != activeBtn) btn.setStyle(UIStyles.BTN_MENU); });
            sidebar.getChildren().add(btn);
        }
        root.setLeft(sidebar);

        contentPane.setPadding(new Insets(24.0));
        contentPane.setStyle("-fx-background-color: #F0F4F8;");
        root.setCenter(contentPane);
        showScreen("home");
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.setStyle(UIStyles.BTN_MENU);
        activeBtn = btn;
        btn.setStyle(UIStyles.BTN_MENU_ACTIVE);
    }

    private void showScreen(String screen) {
        Node view = switch (screen) {
            case "home" -> buildTeacherHome();
            case "profile" -> buildProfileView();
            case "routine" -> new TeacherRoutineView(storage, authService.getCurrentUserId()).getRoot();
            case "availability" -> new TeacherAvailabilityView(storage, authService.getCurrentUserId()).getRoot();
            default -> new Label("Select from menu");
        };
        FadeTransition fade = new FadeTransition(Duration.millis(250), view);
        fade.setFromValue(0); fade.setToValue(1);
        contentPane.getChildren().setAll(view);
        fade.play();
    }

    private Node buildTeacherHome() {
        String tid = authService.getCurrentUserId();
        Optional<Teacher> tOpt = storage.loadTeachers().stream().filter(t -> tid.equals(t.getTeacherId())).findFirst();
        
        VBox v = new VBox(20);
        v.setPadding(new Insets(8.0));

        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(24.0, 30.0, 24.0, 30.0));
        banner.setStyle("-fx-background-color: linear-gradient(to right, #1565C0, #0097A7); -fx-background-radius: 14;");
        VBox bannerText = new VBox(6);
        String name = tOpt.map(Teacher::getName).orElse(tid);
        Label welcome = new Label("Welcome, " + name + "!");
        welcome.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub = new Label("Your teaching dashboard — view your schedule and manage availability.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");
        bannerText.getChildren().addAll(welcome, sub);
        banner.getChildren().add(bannerText);

        v.getChildren().add(banner);

        if (tOpt.isPresent()) {
            Teacher t = tOpt.get();
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(16); statsGrid.setVgap(16);
            statsGrid.getColumnConstraints().addAll(cc(), cc(), cc());
            statsGrid.add(infoCard("🏛", "Department", t.getDepartmentId() != null ? t.getDepartmentId() : "—", "#1565C0"), 0, 0);
            statsGrid.add(infoCard("📚", "Max Credits", String.valueOf(t.getMaxCreditLoad()), "#2E7D32"), 1, 0);
            statsGrid.add(infoCard("📅", "Available Days", String.valueOf(t.getAvailableDays() != null ? t.getAvailableDays().size() : 0), "#AD1457"), 2, 0);
            v.getChildren().add(statsGrid);
        }

        return v;
    }

    private ColumnConstraints cc() {
        ColumnConstraints c = new ColumnConstraints(); c.setHgrow(Priority.ALWAYS); c.setFillWidth(true); return c;
    }

    private VBox infoCard(String icon, String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20.0));
        card.setStyle(UIStyles.STAT_CARD);
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size: 24px;");
        Label valueLbl = new Label(value); valueLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label nameLbl = new Label(label); nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909C;");
        card.getChildren().addAll(iconLbl, valueLbl, nameLbl);
        return card;
    }

    private Node buildProfileView() {
        String tid = authService.getCurrentUserId();
        Optional<Teacher> tOpt = storage.loadTeachers().stream().filter(t -> tid.equals(t.getTeacherId())).findFirst();
        if (tOpt.isEmpty()) return new Label("Profile not found.");
        Teacher t = tOpt.get();

        VBox v = new VBox(14);
        v.setPadding(new Insets(24.0));
        v.setMaxWidth(600);
        v.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3);");

        Label header = new Label("👤  My Profile");
        header.setStyle(UIStyles.TITLE);
        v.getChildren().add(header);
        v.getChildren().add(new Separator());

        String[][] fields = {
            {"ID", t.getTeacherId()}, {"Name", t.getName()}, {"Short Name", t.getShortName()},
            {"Email", t.getEmail() != null ? t.getEmail() : "—"},
            {"Phone", t.getPhone() != null ? t.getPhone() : "—"},
            {"Department", t.getDepartmentId() != null ? t.getDepartmentId() : "—"},
            {"Max Credits", String.valueOf(t.getMaxCreditLoad())},
            {"Program", t.getProgramPreference() != null ? t.getProgramPreference().name() : "—"},
            {"Available Days", t.getAvailableDays() != null ? String.join(", ", t.getAvailableDays()) : "—"},
            {"Time Slots", t.getAvailableTimeSlots() != null ? String.join(", ", t.getAvailableTimeSlots()) : "—"}
        };
        for (String[] field : fields) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            Label key = new Label(field[0] + ":"); key.setStyle("-fx-font-weight: bold; -fx-text-fill: #455A64; -fx-min-width: 140;");
            Label val = new Label(field[1]); val.setStyle("-fx-text-fill: #263238; -fx-font-size: 13px;"); val.setWrapText(true);
            row.getChildren().addAll(key, val);
            v.getChildren().add(row);
        }
        return new ScrollPane(v);
    }

    public BorderPane getRoot() { return root; }
}