package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.FileStorage;
import com.scheduler.util.TimeRules;
import com.scheduler.util.UIStyles;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class TeacherAvailabilityView {
    private final FileStorage storage;
    private final String teacherId;
    private final VBox root;

    public TeacherAvailabilityView(FileStorage storage, String teacherId) {
        this.storage = storage;
        this.teacherId = teacherId;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label title = new Label("⏰  Set My Availability");
        title.setStyle(UIStyles.TITLE);
        titleBar.getChildren().add(title);

        Optional<Teacher> tOpt = storage.loadTeachers().stream().filter(t -> teacherId.equals(t.getTeacherId())).findFirst();
        if (tOpt.isEmpty()) {
            v.getChildren().addAll(titleBar, new Label("Teacher not found."));
            return v;
        }
        Teacher teacher = tOpt.get();

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Days
        Label daysTitle = new Label("📅  Available Days");
        daysTitle.setStyle(UIStyles.SUBTITLE);
        HBox daysBox = new HBox(16);
        daysBox.setPadding(new Insets(12));
        daysBox.setStyle("-fx-background-color: #F5F7FA; -fx-background-radius: 8;");
        String[] allDays = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        List<CheckBox> dayChecks = new ArrayList<>();
        for (String day : allDays) {
            CheckBox cb = new CheckBox(day.substring(0, 3));
            cb.setUserData(day);
            cb.setSelected(teacher.getAvailableDays() != null && teacher.getAvailableDays().contains(day));
            cb.setStyle("-fx-font-size: 13px;");
            dayChecks.add(cb);
            daysBox.getChildren().add(cb);
        }

        // Time slots
        Label slotsTitle = new Label("🕐  Available Time Slots");
        slotsTitle.setStyle(UIStyles.SUBTITLE);
        HBox slotsBox = new HBox(16);
        slotsBox.setPadding(new Insets(12));
        slotsBox.setStyle("-fx-background-color: #F5F7FA; -fx-background-radius: 8;");
        List<CheckBox> slotChecks = new ArrayList<>();
        for (String slot : TimeRules.TEACHING_SLOTS) {
            CheckBox cb = new CheckBox(slot);
            cb.setUserData(slot);
            cb.setSelected(teacher.getAvailableTimeSlots() == null || teacher.getAvailableTimeSlots().isEmpty() || teacher.getAvailableTimeSlots().contains(slot));
            cb.setStyle("-fx-font-size: 13px;");
            slotChecks.add(cb);
            slotsBox.getChildren().add(cb);
        }

        Button saveBtn = new Button("✓  Save Availability");
        saveBtn.setStyle(UIStyles.BTN_SUCCESS + " -fx-font-size: 14px;");
        saveBtn.setOnAction(e -> {
            List<String> selectedDays = dayChecks.stream().filter(CheckBox::isSelected)
                    .map(cb -> cb.getUserData().toString()).collect(Collectors.toList());
            List<String> selectedSlots = slotChecks.stream().filter(CheckBox::isSelected)
                    .map(cb -> cb.getUserData().toString()).collect(Collectors.toList());
            teacher.setAvailableDays(selectedDays);
            teacher.setAvailableTimeSlots(selectedSlots.isEmpty() ? Arrays.asList(TimeRules.TEACHING_SLOTS) : selectedSlots);
            List<Teacher> all = new ArrayList<>(storage.loadTeachers());
            all.replaceAll(t -> t.getTeacherId().equals(teacherId) ? teacher : t);
            storage.saveTeachers(all);
            new Alert(Alert.AlertType.INFORMATION, "✅ Availability saved!").showAndWait();
        });

        card.getChildren().addAll(daysTitle, daysBox, slotsTitle, slotsBox, saveBtn);
        v.getChildren().addAll(titleBar, card);
        return v;
    }

    public VBox getRoot() { return root; }
}
