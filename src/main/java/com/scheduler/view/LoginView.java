package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.storage.FileStorage;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView {
    private final FileStorage storage;
    private final AuthService authService;
    private final Stage stage;
    private final StackPane root;

    private RadioButton adminRadio, teacherRadio;
    private TextField idField;
    private PasswordField passwordField;

    public LoginView(FileStorage storage, AuthService authService, Stage stage) {
        this.storage = storage;
        this.authService = authService;
        this.stage = stage;
        this.root = build();
        animateIn();
    }

    private StackPane build() {
        StackPane pane = new StackPane();
        // Matching the new Splash Screen background
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #F0F4F8, #CFD8DC);");

        // --- CENTER FLOATING CARD ---
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(420, 520);
        card.setPadding(new Insets(45, 50, 45, 50));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        // Soft drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.08));
        shadow.setRadius(25);
        shadow.setOffsetY(10);
        card.setEffect(shadow);

        // --- LOGO (Matching Splash) ---
        StackPane logoPane = new StackPane();
        Rectangle rect1 = new Rectangle(32, 32);
        rect1.setArcWidth(10); rect1.setArcHeight(10);
        rect1.setFill(Color.web("#1565C0"));
        rect1.setTranslateX(-8); rect1.setTranslateY(-8);

        Rectangle rect2 = new Rectangle(32, 32);
        rect2.setArcWidth(10); rect2.setArcHeight(10);
        rect2.setFill(Color.web("#00BCD4"));
        rect2.setOpacity(0.85);
        rect2.setTranslateX(8); rect2.setTranslateY(8);
        logoPane.getChildren().addAll(rect1, rect2);

        // --- TYPOGRAPHY ---
        Label loginTitle = new Label("Welcome Back");
        loginTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #1A237E;");
        VBox.setMargin(loginTitle, new Insets(10, 0, -5, 0));

        Label loginSub = new Label("Please sign in to continue");
        loginSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909C;");
        VBox.setMargin(loginSub, new Insets(0, 0, 15, 0));

        // --- ROLE SELECTION ---
        ToggleGroup role = new ToggleGroup();
        adminRadio = new RadioButton("Administrator");
        teacherRadio = new RadioButton("Teacher");
        adminRadio.setToggleGroup(role); 
        teacherRadio.setToggleGroup(role);
        adminRadio.setSelected(true);
        
        String radioStyle = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #455A64; -fx-cursor: hand;";
        adminRadio.setStyle(radioStyle);
        teacherRadio.setStyle(radioStyle);

        HBox roleBox = new HBox(20, adminRadio, teacherRadio);
        roleBox.setAlignment(Pos.CENTER);
        VBox.setMargin(roleBox, new Insets(0, 0, 10, 0));

        // --- INPUT FIELDS ---
        String inputStyle = "-fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 12 15; -fx-font-size: 14px; -fx-background-color: #FAFAFA;";
        String inputFocusStyle = "-fx-background-radius: 8; -fx-border-color: #1565C0; -fx-border-radius: 8; -fx-padding: 12 15; -fx-font-size: 14px; -fx-background-color: white; -fx-effect: dropshadow(gaussian, #1565C033, 6, 0, 0, 0);";
        
        VBox idContainer = new VBox(5);
        Label idLabel = new Label("User ID");
        idLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #546E7A;");
        idField = new TextField();
        idField.setPromptText("Enter your ID");
        idField.setStyle(inputStyle);
        idField.focusedProperty().addListener((obs, oldVal, newVal) -> idField.setStyle(newVal ? inputFocusStyle : inputStyle));
        idContainer.getChildren().addAll(idLabel, idField);

        VBox passContainer = new VBox(5);
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #546E7A;");
        passwordField = new PasswordField();
        passwordField.setPromptText("••••••••");
        passwordField.setStyle(inputStyle);
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> passwordField.setStyle(newVal ? inputFocusStyle : inputStyle));
        passContainer.getChildren().addAll(passLabel, passwordField);

        // --- LOGIN BUTTON ---
        Button loginBtn = new Button("Sign In");
        loginBtn.setDefaultButton(true);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setCursor(javafx.scene.Cursor.HAND);
        String btnNormal = "-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 14; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(21,101,192,0.3), 8, 0, 0, 4);";
        String btnHover = "-fx-background-color: #0D47A1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 14; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(13,71,161,0.4), 8, 0, 0, 4);";
        
        loginBtn.setStyle(btnNormal);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(btnHover));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(btnNormal));
        loginBtn.setOnAction(e -> doLogin());
        VBox.setMargin(loginBtn, new Insets(15, 0, 0, 0));

        card.getChildren().addAll(logoPane, loginTitle, loginSub, roleBox, idContainer, passContainer, loginBtn);
        
        // Save card reference for animation
        pane.getChildren().add(card);
        pane.getProperties().put("card", card);

        return pane;
    }

    private void animateIn() {
        VBox card = (VBox) root.getProperties().get("card");
        
        root.setOpacity(0);
        card.setTranslateY(20);

        FadeTransition rootFade = new FadeTransition(Duration.millis(500), root);
        rootFade.setToValue(1);

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(600), card);
        cardSlide.setToY(0);
        cardSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(rootFade, cardSlide);
        pt.play();
    }

    private void doLogin() {
        String id = idField.getText() == null ? "" : idField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText();

        if (id.isEmpty() || pass.isEmpty()) {
            showAlert("Login Required", "Please enter both User ID and Password.");
            shakeNode(id.isEmpty() ? idField : passwordField);
            return;
        }

        boolean ok = adminRadio.isSelected()
                ? authService.loginAdmin(id, pass)
                : authService.loginTeacher(id, pass);

        if (ok) {
            idField.clear();
            passwordField.clear();
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                if (authService.isAdmin()) {
                    AdminDashboard admin = new AdminDashboard(storage, authService, stage);
                    Scene scene = new Scene(admin.getRoot(), 1366, 768);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.centerOnScreen();
                } else {
                    TeacherDashboard teacher = new TeacherDashboard(storage, authService, stage);
                    Scene scene = new Scene(teacher.getRoot(), 1366, 768);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.centerOnScreen();
                }
            });
            fadeOut.play();
        } else {
            showAlert("Invalid Credentials", "The ID or Password you entered is incorrect.");
            shakeNode(root.getProperties().get("card") != null ? (Node) root.getProperties().get("card") : root);
            idField.clear();
            passwordField.clear();
            idField.requestFocus();
        }
    }

    private void shakeNode(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void showAlert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Authentication Error");
        a.setHeaderText(header);
        a.setContentText(content);
        
        DialogPane dialogPane = a.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        a.showAndWait();
    }

    public StackPane getRoot() { return root; }
}