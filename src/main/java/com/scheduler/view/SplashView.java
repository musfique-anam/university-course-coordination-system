package com.scheduler.view;

import com.scheduler.auth.AuthService;
import com.scheduler.storage.DatabaseStorage; // <-- The missing import!
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashView {

    private final DatabaseStorage storage; // <-- Updated
    private final AuthService authService;
    private final Stage stage;
    private final StackPane root;

    // <-- Updated Constructor
    public SplashView(DatabaseStorage storage, AuthService authService, Stage stage) {
        this.storage = storage;
        this.authService = authService;
        this.stage = stage;
        this.root = build();
        startAnimation();
    }

    private StackPane build() {
        StackPane pane = new StackPane();
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #2B2A33, #1A1A20);");
        pane.setPrefSize(900, 600);

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(450, 350);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: #383742; -fx-background-radius: 16;");
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(25);
        shadow.setOffsetY(12);
        card.setEffect(shadow);

        StackPane logoPane = new StackPane();
        
        Rectangle rect1 = new Rectangle(40, 40);
        rect1.setArcWidth(12); rect1.setArcHeight(12);
        rect1.setFill(Color.web("#36B1BF")); 
        rect1.setTranslateX(-10); rect1.setTranslateY(-10);

        Rectangle rect2 = new Rectangle(40, 40);
        rect2.setArcWidth(12); rect2.setArcHeight(12);
        rect2.setFill(Color.web("#F2A900")); 
        rect2.setOpacity(0.9);
        rect2.setTranslateX(10); rect2.setTranslateY(10);

        logoPane.getChildren().addAll(rect1, rect2);

        Label appName = new Label("Academic Scheduler");
        appName.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;"); 
        
        Label subtitle = new Label("Intelligent Timetable Generation");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #A6A5B5; -fx-font-weight: 600;"); 

        Label version = new Label("v2.0 \u2022 Designed by Anonto & Arif");
        version.setStyle("-fx-font-size: 11px; -fx-text-fill: #737280;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: #36B1BF; " + 
            "-fx-control-inner-background: #25242C; " + 
            "-fx-background-color: transparent; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 0;"
        );

        VBox textGroup = new VBox(8, appName, subtitle);
        textGroup.setAlignment(Pos.CENTER);

        card.getChildren().addAll(logoPane, textGroup, progressBar, version);
        pane.getChildren().add(card);

        pane.getProperties().put("card", card);
        pane.getProperties().put("rect1", rect1);
        pane.getProperties().put("rect2", rect2);
        pane.getProperties().put("textGroup", textGroup);
        pane.getProperties().put("progressBar", progressBar);
        pane.getProperties().put("version", version);

        card.setOpacity(0);
        card.setTranslateY(30);
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

            FadeTransition cardFade = new FadeTransition(Duration.millis(600), card);
            cardFade.setToValue(1.0);
            
            TranslateTransition cardSlide = new TranslateTransition(Duration.millis(600), card);
            cardSlide.setToY(0);
            cardSlide.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition cardEntry = new ParallelTransition(cardFade, cardSlide);

            RotateTransition rot1 = new RotateTransition(Duration.millis(800), rect1);
            rot1.setFromAngle(-45); rot1.setToAngle(0);
            rot1.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rot2 = new RotateTransition(Duration.millis(800), rect2);
            rot2.setFromAngle(45); rot2.setToAngle(0);
            rot2.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition textFade = new FadeTransition(Duration.millis(500), textGroup);
            textFade.setToValue(1.0);

            FadeTransition barFade = new FadeTransition(Duration.millis(300), progressBar);
            barFade.setToValue(1.0);

            Timeline progressTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                    new KeyFrame(Duration.millis(1500), new KeyValue(progressBar.progressProperty(), 1.0, Interpolator.EASE_BOTH))
            );

            FadeTransition versionFade = new FadeTransition(Duration.millis(400), version);
            versionFade.setToValue(1.0);

            SequentialTransition mainSequence = new SequentialTransition(
                    cardEntry,
                    new ParallelTransition(rot1, rot2, textFade),
                    barFade,
                    new ParallelTransition(progressTimeline, versionFade)
            );

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

    public StackPane getRoot() { return root; }
}