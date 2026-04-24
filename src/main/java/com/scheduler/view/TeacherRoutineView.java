package com.scheduler.view;

import com.scheduler.model.RoutineEntry;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.util.UIStyles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class TeacherRoutineView {
    private final DatabaseStorage storage;
    private final String teacherId;
    private final VBox root;

    public TeacherRoutineView(DatabaseStorage storage, String teacherId) {
        this.storage = storage;
        this.teacherId = teacherId;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label title = new Label("📅  My Assigned Routine");
        title.setStyle(UIStyles.TITLE);

        List<RoutineEntry> mine = storage.loadRoutine().stream().filter(e -> teacherId.equals(e.getTeacherId())).toList();

        Label countBadge = new Label(mine.size() + " classes");
        countBadge.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button downloadBtn = new Button("⬇ Download");
        downloadBtn.setStyle(UIStyles.BTN_PRIMARY + "-fx-padding: 8 16;");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, countBadge, sp, downloadBtn);

        var list = FXCollections.observableArrayList(mine);
        TableView<RoutineEntry> table = new TableView<>(list);
        TableColumn<RoutineEntry, String> dayCol = col("Day", c -> new SimpleStringProperty(c.getValue().getDay()));
        TableColumn<RoutineEntry, String> slotCol = col("Time Slot", c -> new SimpleStringProperty(c.getValue().getTimeSlot()));
        TableColumn<RoutineEntry, String> courseCol = col("Course", c -> new SimpleStringProperty(c.getValue().getCourseCode() + " " + (c.getValue().getCourseTitle() != null ? c.getValue().getCourseTitle() : "")));
        courseCol.setPrefWidth(200);
        TableColumn<RoutineEntry, String> roomCol = col("Room", c -> new SimpleStringProperty(c.getValue().getRoomNo() != null ? c.getValue().getRoomNo() : "—"));
        TableColumn<RoutineEntry, String> deptCol = col("Dept", c -> new SimpleStringProperty(c.getValue().getDepartmentId() != null ? c.getValue().getDepartmentId() : ""));
        TableColumn<RoutineEntry, String> batchesCol = col("Batch(es)", c -> new SimpleStringProperty(String.join(", ", c.getValue().getBatchNames())));
        table.getColumns().addAll(dayCol, slotCol, courseCol, roomCol, deptCol, batchesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(400);

        downloadBtn.setOnAction(e -> downloadRoutine(mine));

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, tableCard);
        return v;
    }

    private TableColumn<RoutineEntry, String> col(String name, javafx.util.Callback<TableColumn.CellDataFeatures<RoutineEntry, String>, javafx.beans.value.ObservableValue<String>> f) {
        TableColumn<RoutineEntry, String> c = new TableColumn<>(name);
        c.setCellValueFactory(f);
        return c;
    }

    private void downloadRoutine(List<RoutineEntry> entries) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fc.setInitialFileName("my_routine_" + teacherId + ".txt");
        java.io.File f = fc.showSaveDialog(root.getScene() != null ? root.getScene().getWindow() : null);
        if (f == null) return;
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(f.toPath()))) {
            w.println("=" .repeat(70));
            w.println("PERSONAL ROUTINE — Teacher: " + teacherId);
            w.println("=".repeat(70));
            w.printf("%-12s %-15s %-30s %-10s %-12s%n", "Day", "Time", "Course", "Room", "Batch");
            w.println("-".repeat(80));
            for (RoutineEntry e : entries) {
                w.printf("%-12s %-15s %-30s %-10s %-12s%n",
                        e.getDay(), e.getTimeSlot(),
                        (e.getCourseCode() != null ? e.getCourseCode() : "") + " " + (e.getCourseTitle() != null ? e.getCourseTitle() : ""),
                        e.getRoomNo() != null ? e.getRoomNo() : "—",
                        String.join(",", e.getBatchNames()));
            }
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Failed: " + ex.getMessage()).showAndWait();
        }
    }

    public VBox getRoot() { return root; }
}