package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.storage.FileStorage;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashView {

    private final FileStorage storage;
    private final AuthService authService;
    private final Stage stage;
    private final StackPane root;

    public SplashView(FileStorage storage, AuthService authService, Stage stage) {
        this.storage = storage;
        this.authService = authService;
        this.stage = stage;
        this.root = build();
        startAnimation();
    }

    private StackPane build() {
        StackPane pane = new StackPane();
        // Modern subtle gradient background
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #F0F4F8, #CFD8DC);");
        pane.setPrefSize(900, 600);

        // --- Center Card ---
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(450, 350);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        
        // Soft drop shadow for the card
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(20);
        shadow.setOffsetY(10);
        card.setEffect(shadow);

        // --- Animated Logo Concept (Overlapping shapes) ---
        StackPane logoPane = new StackPane();
        
        Rectangle rect1 = new Rectangle(40, 40);
        rect1.setArcWidth(12);
        rect1.setArcHeight(12);
        rect1.setFill(Color.web("#1565C0")); // Primary Blue
        rect1.setTranslateX(-10);
        rect1.setTranslateY(-10);

        Rectangle rect2 = new Rectangle(40, 40);
        rect2.setArcWidth(12);
        rect2.setArcHeight(12);
        rect2.setFill(Color.web("#00BCD4")); // Accent Cyan
        rect2.setOpacity(0.85);
        rect2.setTranslateX(10);
        rect2.setTranslateY(10);

        logoPane.getChildren().addAll(rect1, rect2);

        // --- Text Elements ---
        Label appName = new Label("Academic Scheduler");
        appName.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1A237E;");
        
        Label subtitle = new Label("Intelligent Timetable Generation");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #546E7A; -fx-font-weight: 600;");

        Label version = new Label("v2.0 \u2022 Designed by Anonto & Arif");
        version.setStyle("-fx-font-size: 11px; -fx-text-fill: #90A4AE;");

        // --- Modern Progress Bar ---
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(6);
        // Styling the progress bar to remove the default chunky look
        progressBar.setStyle(
            "-fx-accent: #1565C0; " +
            "-fx-control-inner-background: #ECEFF1; " +
            "-fx-background-color: transparent; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 0;"
        );

        // Grouping text to animate together
        VBox textGroup = new VBox(8, appName, subtitle);
        textGroup.setAlignment(Pos.CENTER);

        card.getChildren().addAll(logoPane, textGroup, progressBar, version);
        pane.getChildren().add(card);

        // Store references for the animation method
        pane.getProperties().put("card", card);
        pane.getProperties().put("rect1", rect1);
        pane.getProperties().put("rect2", rect2);
        pane.getProperties().put("textGroup", textGroup);
        pane.getProperties().put("progressBar", progressBar);
        pane.getProperties().put("version", version);

        // Initial setup for animation (hide elements)
        card.setOpacity(0);
        card.setTranslateY(30); // Start slightly lower
        textGroup.setOpacity(0);
        progressBar.setOpacity(0);
        version.setOpacity(0);

        return pane;
    }

    private void startAnimation() {
        javafx.application.Platform.runLater(() -> {
            VBox card = (VBox) root.getProperties().get("card");
            Rectangle rect1 = (Rectangle) root.getProperties().get("rect1");
            Rectangle rect2 = (Rectangle) root.getProperties().get("rect2");
            VBox textGroup = (VBox) root.getProperties().get("textGroup");
            ProgressBar progressBar = (ProgressBar) root.getProperties().get("progressBar");
            Label version = (Label) root.getProperties().get("version");

            // 1. Card Fades and Slides Up
            FadeTransition cardFade = new FadeTransition(Duration.millis(600), card);
            cardFade.setToValue(1.0);
            
            TranslateTransition cardSlide = new TranslateTransition(Duration.millis(600), card);
            cardSlide.setToY(0);
            cardSlide.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition cardEntry = new ParallelTransition(cardFade, cardSlide);

            // 2. Logo Animation (Shapes rotate into place)
            RotateTransition rot1 = new RotateTransition(Duration.millis(800), rect1);
            rot1.setFromAngle(-45);
            rot1.setToAngle(0);
            rot1.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rot2 = new RotateTransition(Duration.millis(800), rect2);
            rot2.setFromAngle(45);
            rot2.setToAngle(0);
            rot2.setInterpolator(Interpolator.EASE_OUT);

            // 3. Text Fades In
            FadeTransition textFade = new FadeTransition(Duration.millis(500), textGroup);
            textFade.setToValue(1.0);

            // 4. Progress Bar Fades in and Loads
            FadeTransition barFade = new FadeTransition(Duration.millis(300), progressBar);
            barFade.setToValue(1.0);

            Timeline progressTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                    new KeyFrame(Duration.millis(1500), new KeyValue(progressBar.progressProperty(), 1.0, Interpolator.EASE_BOTH))
            );

            // 5. Version label fades in last
            FadeTransition versionFade = new FadeTransition(Duration.millis(400), version);
            versionFade.setToValue(1.0);

            // --- Sequence Choreography ---
            SequentialTransition mainSequence = new SequentialTransition(
                    cardEntry,
                    new ParallelTransition(rot1, rot2, textFade),
                    barFade,
                    new ParallelTransition(progressTimeline, versionFade)
            );

            // When animation finishes, wait a tiny bit, then fade out the whole screen and go to login
            mainSequence.setOnFinished(e -> {
                PauseTransition pause = new PauseTransition(Duration.millis(400));
                pause.setOnFinished(pe -> {
                    FadeTransition rootFadeOut = new FadeTransition(Duration.millis(500), root);
                    rootFadeOut.setToValue(0);
                    rootFadeOut.setOnFinished(fadeOutEvent -> showLogin());
                    rootFadeOut.play();
                });
                pause.play();
            });

            mainSequence.play();
        });
    }

    private void showLogin() {
        LoginView loginView = new LoginView(storage, authService, stage);
        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public StackPane getRoot() {
        return root;
    }
}