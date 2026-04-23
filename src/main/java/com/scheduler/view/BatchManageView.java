package com.scheduler.view;

import com.scheduler.model.*;
import com.scheduler.storage.FileStorage;
import com.scheduler.util.UIStyles;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;

import java.util.*;

public class BatchManageView {
    private final FileStorage storage;
    private final ObservableList<Batch> list = FXCollections.observableArrayList();
    private FilteredList<Batch> filteredList;
    private final TableView<Batch> table = new TableView<>();
    private final VBox root;

    public BatchManageView(FileStorage storage) {
        this.storage = storage;
        this.root = build();
    }

    private VBox build() {
        VBox v = new VBox(20);
        v.setPadding(new Insets(20));
        v.setStyle(UIStyles.BG_MAIN);

        // Title
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label title = new Label("📚  Batch Management");
        title.setStyle(UIStyles.TITLE);
        titleBar.getChildren().add(title);

        // Filter card
        VBox filterCard = new VBox(12);
        filterCard.setPadding(new Insets(16, 20, 16, 20));
        filterCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        Label filterTitle = new Label("🔍  Filter & View Batches");
        filterTitle.setStyle(UIStyles.SUBTITLE);

        ComboBox<String> deptFilter = new ComboBox<>();
        deptFilter.getItems().add("All Departments");
        storage.loadDepartments().forEach(d -> deptFilter.getItems().add(d.getId() + " — " + d.getName()));
        deptFilter.setValue("All Departments");
        deptFilter.setStyle(UIStyles.COMBO);
        deptFilter.setPrefWidth(280);

        ComboBox<String> progFilter = new ComboBox<>();
        progFilter.getItems().addAll("All Programs", "HSC", "DIPLOMA");
        progFilter.setValue("All Programs");
        progFilter.setStyle(UIStyles.COMBO);

        Button addBtn = new Button("+ Add Batch");
        addBtn.setStyle(UIStyles.BTN_SUCCESS);
        addBtn.setOnAction(e -> showBatchDialog(null));

        HBox filterRow = new HBox(16, new Label("Department:"), deptFilter, new Label("Program:"), progFilter, new Region(), addBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(filterRow.getChildren().get(5), Priority.ALWAYS);
        filterCard.getChildren().addAll(filterTitle, filterRow);

        // Table
        TableColumn<Batch, String> nameCol = col("Batch Name", c -> new SimpleStringProperty(c.getValue().getName()));
        TableColumn<Batch, String> deptCol = col("Department", c -> new SimpleStringProperty(c.getValue().getDepartmentId()));
        TableColumn<Batch, String> progCol = col("Program", c -> new SimpleStringProperty(c.getValue().getProgramType() != null ? c.getValue().getProgramType().name() : ""));
        TableColumn<Batch, String> studCol = col("Students", c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalStudents())));
        TableColumn<Batch, String> coursesCol = col("Courses", c -> new SimpleStringProperty(String.valueOf(c.getValue().getCourses().size())));

        TableColumn<Batch, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(220);
        actCol.setCellFactory(cc -> new TableCell<>() {
            private final Button viewBtn = new Button("👁 View");
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑");
            {
                viewBtn.setStyle(UIStyles.BTN_PRIMARY + "-fx-font-size: 11px; -fx-padding: 5 10;");
                editBtn.setStyle(UIStyles.BTN_WARNING + "-fx-font-size: 11px; -fx-padding: 5 10;");
                delBtn.setStyle(UIStyles.BTN_DANGER + "-fx-font-size: 11px; -fx-padding: 5 10;");
                viewBtn.setOnAction(e -> viewBatch(getTableRow().getItem()));
                editBtn.setOnAction(e -> showBatchDialog(getTableRow().getItem()));
                delBtn.setOnAction(e -> deleteBatch(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, viewBtn, editBtn, delBtn));
            }
        });

        table.getColumns().addAll(nameCol, deptCol, progCol, studCol, coursesCol, actCol);
        filteredList = new FilteredList<>(list, b -> true);
        table.setItems(filteredList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(UIStyles.TABLE);
        table.setPrefHeight(400);

        deptFilter.setOnAction(e -> applyFilter(deptFilter.getValue(), progFilter.getValue()));
        progFilter.setOnAction(e -> applyFilter(deptFilter.getValue(), progFilter.getValue()));

        VBox tableCard = new VBox(table);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        v.getChildren().addAll(titleBar, filterCard, tableCard);
        refresh();
        return v;
    }

    private TableColumn<Batch, String> col(String name, javafx.util.Callback<TableColumn.CellDataFeatures<Batch, String>, javafx.beans.value.ObservableValue<String>> factory) {
        TableColumn<Batch, String> c = new TableColumn<>(name);
        c.setCellValueFactory(factory);
        return c;
    }

    private void applyFilter(String dept, String prog) {
        filteredList.setPredicate(b -> {
            boolean deptOk = dept == null || dept.equals("All Departments") || b.getDepartmentId().equals(dept.split(" — ")[0]);
            boolean progOk = prog == null || prog.equals("All Programs") || (b.getProgramType() != null && b.getProgramType().name().equals(prog));
            return deptOk && progOk;
        });
    }

    private void viewBatch(Batch b) {
        if (b == null) return;

        Dialog<Void> d = new Dialog<>();
        d.setTitle("Batch Details: " + b.getName());
        d.setHeaderText(b.getName() + " — " + b.getDepartmentId() + " (" + b.getProgramType() + ")");

        TableView<Course> ct = new TableView<>();
        ct.setEditable(true);
        ct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> cc = new TableColumn<>("Code");
        cc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId())); // fixed
        cc.setCellFactory(TextFieldTableCell.forTableColumn());
        cc.setOnEditCommit(event -> {
            String val = event.getNewValue().trim();
            if (!val.isEmpty()) event.getRowValue().setId(val); // fixed
            else event.getRowValue().setId(event.getOldValue());
            storage.saveBatches(list);
            table.refresh();
        });

        TableColumn<Course, String> ct2 = new TableColumn<>("Title");
        ct2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        ct2.setCellFactory(TextFieldTableCell.forTableColumn());
        ct2.setOnEditCommit(event -> {
            String val = event.getNewValue().trim();
            if (!val.isEmpty()) event.getRowValue().setTitle(val);
            else event.getRowValue().setTitle(event.getOldValue());
            storage.saveBatches(list);
            table.refresh();
        });

        TableColumn<Course, Integer> cr = new TableColumn<>("Credit");
        cr.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCredit()).asObject());
        cr.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        cr.setOnEditCommit(event -> {
            int val = event.getNewValue() != null && event.getNewValue() > 0 ? event.getNewValue() : event.getOldValue();
            event.getRowValue().setCredit(val);
            storage.saveBatches(list);
            table.refresh();
        });

        TableColumn<Course, String> ctp = new TableColumn<>("Type");
        ctp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseType().name()));
        ctp.setCellFactory(ComboBoxTableCell.forTableColumn("THEORY", "LAB"));
        ctp.setOnEditCommit(event -> {
            String val = event.getNewValue();
            event.getRowValue().setCourseType("LAB".equals(val) ? CourseType.LAB : CourseType.THEORY);
            storage.saveBatches(list);
            table.refresh();
        });

        ct.getColumns().addAll(cc, ct2, cr, ctp);
        ct.getItems().addAll(b.getCourses());
        ct.setPrefHeight(300);

        VBox content = new VBox(12,
                new Label("Students: " + b.getTotalStudents()),
                new Label("Courses (" + b.getCourses().size() + "):"),
                ct
        );
        content.setPadding(new Insets(16));

        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.getDialogPane().setPrefWidth(600);
        d.showAndWait();
    }

    // =================== ADD / EDIT BATCH ===================
    private void showBatchDialog(Batch batch) {
        boolean isEdit = batch != null;
        Dialog<Boolean> d = new Dialog<>();
        d.setTitle(isEdit ? "Edit Batch: " + batch.getName() : "Add New Batch");
        d.getDialogPane().setPrefSize(700, 650);

        List<Department> depts = storage.loadDepartments();

        ComboBox<String> deptCombo = new ComboBox<>(FXCollections.observableArrayList(
                depts.stream().map(dep -> dep.getId() + " — " + dep.getName()).toList()));
        deptCombo.setStyle(UIStyles.COMBO);
        deptCombo.setPrefWidth(300);
        if (!depts.isEmpty()) deptCombo.getSelectionModel().select(0);

        TextField nameField = new TextField(isEdit ? batch.getName() : "");
        nameField.setPromptText("e.g. 58th Batch, Batch 2024");
        nameField.setStyle(UIStyles.INPUT);
        nameField.setPrefWidth(250);

        ComboBox<String> programCombo = new ComboBox<>(FXCollections.observableArrayList("HSC", "DIPLOMA"));
        programCombo.setStyle(UIStyles.COMBO);
        programCombo.getSelectionModel().select(isEdit && batch.getProgramType() == ProgramType.DIPLOMA ? 1 : 0);

        Spinner<Integer> studentsSpinner = new Spinner<>(1, 1000, isEdit ? batch.getTotalStudents() : 60);
        studentsSpinner.setEditable(true);
        studentsSpinner.setPrefWidth(120);

        // Courses
        List<CourseInput> courseInputs = new ArrayList<>();
        VBox courseBox = new VBox(8);
        courseBox.setPadding(new Insets(8));

        Button addCourseBtn = new Button("+ Add Course Row");
        addCourseBtn.setStyle(UIStyles.BTN_OUTLINE);
        addCourseBtn.setOnAction(e2 -> addCourseRow(courseBox, courseInputs, null));

        if (isEdit) batch.getCourses().forEach(c -> addCourseRow(courseBox, courseInputs, c));

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        form.setPadding(new Insets(16));

        form.add(lbl("Department *"), 0, 0); form.add(deptCombo, 1, 0);
        form.add(lbl("Batch Name *"), 0, 1); form.add(nameField, 1, 1);
        form.add(lbl("Program *"), 0, 2); form.add(programCombo, 1, 2);
        form.add(lbl("Students"), 0, 3); form.add(studentsSpinner, 1, 3);

        Separator sep = new Separator();
        Label courseTitle = new Label("📖  Courses");
        courseTitle.setStyle(UIStyles.SUBTITLE);

        ScrollPane courseScroll = new ScrollPane(courseBox);
        courseScroll.setFitToWidth(true);
        courseScroll.setPrefHeight(200);

        VBox content = new VBox(12, form, sep, courseTitle, addCourseBtn, courseScroll);
        content.setPadding(new Insets(8));
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            String name = nameField.getText() != null ? nameField.getText().trim() : "";
            String deptSel = deptCombo.getValue();
            if (name.isEmpty() || deptSel == null) {
                new Alert(Alert.AlertType.WARNING, "Department and Batch Name required!").showAndWait();
                return null;
            }

            String deptId = deptSel.split(" — ")[0];
            ProgramType prog = "DIPLOMA".equals(programCombo.getValue()) ? ProgramType.DIPLOMA : ProgramType.HSC;

            if (courseInputs.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Add at least one course!").showAndWait();
                return null;
            }

            List<Course> courses = new ArrayList<>();
            for (CourseInput ci : courseInputs) {
                String ccode = ci.code.getText().trim();
                String ctitle = ci.title.getText().trim();
                if (ccode.isEmpty() || ctitle.isEmpty()) continue;
                CourseType ctype = "LAB".equals(ci.type.getValue()) ? CourseType.LAB : CourseType.THEORY;
                Course c = new Course(ccode, ctitle, ci.credit.getValue(), ctype, deptId, prog,
                        isEdit ? batch.getId() : UUID.randomUUID().toString());
                c.setBatchId(isEdit ? batch.getId() : UUID.randomUUID().toString());
                courses.add(c);
            }

            if (isEdit) {
                batch.setName(name); batch.setDepartmentId(deptId);
                batch.setProgramType(prog); batch.setTotalStudents(studentsSpinner.getValue());
                batch.setCourses(courses);
                storage.saveBatches(list);
                table.refresh();
            } else {
                Batch b = new Batch();
                b.setId(UUID.randomUUID().toString());
                b.setName(name); b.setDepartmentId(deptId);
                b.setProgramType(prog); b.setTotalStudents(studentsSpinner.getValue());
                b.setCourses(courses);
                list.add(b);
                storage.saveBatches(list);
            }
            return true;
        });

        d.showAndWait();
    }

    private void addCourseRow(VBox courseBox, List<CourseInput> courseInputs, Course c) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6.0, 8.0, 6.0, 8.0));
        row.setStyle("-fx-background-color: #F8FAFB; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");

        TextField code = new TextField(c != null ? c.getId() : ""); // fixed
        code.setPromptText("Code"); code.setPrefWidth(90); code.setStyle(UIStyles.INPUT);

        TextField title = new TextField(c != null ? c.getTitle() : "");
        title.setPromptText("Course Title"); title.setPrefWidth(220); title.setStyle(UIStyles.INPUT);

        Spinner<Integer> credit = new Spinner<>(1, 4, c != null ? c.getCredit() : 3);
        credit.setEditable(true); credit.setPrefWidth(75);

        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("THEORY", "LAB"));
        type.setStyle("-fx-min-width: 90px;"); type.getSelectionModel().select(c != null && c.getCourseType() == CourseType.LAB ? 1 : 0);

        Button del = new Button("✕");
        del.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 10;");

        CourseInput ci = new CourseInput(code, title, credit, type);
        courseInputs.add(ci);
        row.getChildren().addAll(new Label("Code:"), code, new Label("Title:"), title, new Label("Cr:"), credit, new Label("Type:"), type, del);
        del.setOnAction(e -> { courseBox.getChildren().remove(row); courseInputs.remove(ci); });
        courseBox.getChildren().add(row);
    }

    private Label lbl(String text) { Label l = new Label(text); l.setStyle(UIStyles.LABEL_FORM); return l; }

    private static class CourseInput { TextField code, title; Spinner<Integer> credit; ComboBox<String> type;
        CourseInput(TextField code, TextField title, Spinner<Integer> credit, ComboBox<String> type) { this.code = code; this.title = title; this.credit = credit; this.type = type; }
    }

    private void deleteBatch(Batch b) {
        if (b == null) return;
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Delete batch: " + b.getName() + "?");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { list.remove(b); storage.saveBatches(list); }
        });
    }

    private void refresh() { list.clear(); list.addAll(storage.loadBatches()); }

    public VBox getRoot() { return root; }
}
