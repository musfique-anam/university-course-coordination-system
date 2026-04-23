package com.scheduler.storage;

import com.scheduler.model.*;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin adapter used by UI. Keeps DataStore consistent and persists via StorageService.
 * All methods needed by views (RoutineView, TeacherManageView, MergeClassView, etc.) are included.
 */
public class FileStorage {

    private final StorageService storage;
    private static final String AUTO_SAVE_FILE = "data/auto_save.json";

    public FileStorage() {
        this.storage = new StorageService();
        // Try to load auto-saved data on startup
        loadAutoSaveData();
    }

    // ---- Auto-save on startup ----
    private void loadAutoSaveData() {
        try {
            File autoSaveFile = new File(AUTO_SAVE_FILE);
            if (autoSaveFile.exists()) {
                storage.loadFromFile(autoSaveFile);
                System.out.println("✅ Auto-save data loaded from: " + AUTO_SAVE_FILE);
            } else {
                System.out.println("ℹ️ No auto-save file found. Starting with fresh data.");
                // Load initial seed data if exists
                loadSeedDataIfExists();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error loading auto-save data: " + e.getMessage());
        }
    }

    private void loadSeedDataIfExists() {
        // Try multiple possible locations for seed data
        String[] possiblePaths = {
            "resources/data_seed.json",
            "data/seed_data.json",
            "data/data_seed.json",
            "seed_data.json",
            "data_seed.json"
        };
        
        for (String path : possiblePaths) {
            File seedFile = new File(path);
            if (seedFile.exists()) {
                try {
                    storage.loadFromFile(seedFile);
                    System.out.println("✅ Seed data loaded from: " + seedFile.getPath());
                    
                    // Save to auto-save location
                    saveAllToJson();
                    return;
                } catch (Exception e) {
                    System.err.println("⚠️ Error loading seed data from " + path + ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("ℹ️ No seed data found. Starting with empty database.");
    }

    // ---- JSON Loading Methods ----
    
    /**
     * Load data from a JSON file selected by the user
     */
    public boolean loadFromJson(File file) {
        System.out.println("📂 FileStorage.loadFromJson called with file: " + file.getAbsolutePath());
        System.out.println("📂 File exists: " + file.exists());
        System.out.println("📂 File size: " + file.length() + " bytes");
        
        if (!file.exists()) {
            System.err.println("❌ File does not exist: " + file.getAbsolutePath());
            return false;
        }
        
        try {
            storage.loadFromFile(file);
            System.out.println("✅ storage.loadFromFile completed successfully");
            
            // After loading, save as new auto-save
            saveAllToJson();
            
            System.out.println("✅ Data successfully loaded from: " + file.getName());
            return true;
        } catch (IOException e) {
            System.err.println("❌ IO Error loading JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error loading JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Open file chooser for user to select JSON file
     */
    public File chooseJsonFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Data from JSON");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        // Set initial directory
        File initialDir = new File("resources");
        if (!initialDir.exists()) {
            initialDir = new File("data");
        }
        if (!initialDir.exists()) {
            initialDir = new File(".");
        }
        
        fileChooser.setInitialDirectory(initialDir);
        
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Save all current data to a JSON file
     */
    public boolean saveToJson(File file) {
        try {
            storage.saveToFile(file);
            System.out.println("✅ Data successfully saved to: " + file.getName());
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Auto-save all data to the default location
     */
    public void saveAllToJson() {
        try {
            File autoSaveFile = new File(AUTO_SAVE_FILE);
            // Create parent directories if they don't exist
            if (autoSaveFile.getParentFile() != null) {
                autoSaveFile.getParentFile().mkdirs();
            }
            storage.saveToFile(autoSaveFile);
            System.out.println("💾 Auto-save completed: " + AUTO_SAVE_FILE);
        } catch (IOException e) {
            System.err.println("❌ Auto-save failed: " + e.getMessage());
        }
    }

    /**
     * Export data with timestamp (backup)
     */
    public boolean exportBackup() {
        try {
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            File backupFile = new File("data/backup_" + timestamp + ".json");
            if (backupFile.getParentFile() != null) {
                backupFile.getParentFile().mkdirs();
            }
            storage.saveToFile(backupFile);
            System.out.println("💾 Backup created: " + backupFile.getName());
            return true;
        } catch (IOException e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            return false;
        }
    }

    // ---- Departments ----
    public List<Department> loadDepartments() {
        return Collections.unmodifiableList(storage.getStore().getDepartments());
    }

    public void saveDepartments(List<Department> depts) {
        var s = storage.getStore();
        s.getDepartments().clear();
        if (depts != null) s.getDepartments().addAll(depts);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    public void saveDepartments(ObservableList<Department> depts) {
        saveDepartments(new ArrayList<>(depts));
    }

    // ---- Courses ----
    
    /**
     * Load all courses from all batches
     */
    public List<Course> loadCourses() {
        List<Course> allCourses = new ArrayList<>();
        List<Batch> batches = loadBatches();
        System.out.println("Loading courses from " + batches.size() + " batches");
        
        for (Batch batch : batches) {
            if (batch.getCourses() != null) {
                List<Course> batchCourses = batch.getCourses();
                System.out.println("  Batch " + batch.getName() + ": " + batchCourses.size() + " courses");
                allCourses.addAll(batchCourses);
            } else {
                System.out.println("  Batch " + batch.getName() + " has null courses!");
            }
        }
        
        System.out.println("Total courses loaded: " + allCourses.size());
        return allCourses;
    }

    /**
     * Save a single course
     */
    public void saveCourse(Course course) {
        List<Batch> batches = loadBatches();
        boolean found = false;
        
        for (Batch batch : batches) {
            List<Course> batchCourses = batch.getCourses();
            if (batchCourses != null) {
                for (int i = 0; i < batchCourses.size(); i++) {
                    Course c = batchCourses.get(i);
                    if (c.getId().equals(course.getId())) {
                        batchCourses.set(i, course);
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        
        if (found) {
            saveBatches(batches);
            System.out.println("✅ Course saved: " + course.getId());
        } else {
            System.out.println("❌ Course not found: " + course.getId());
        }
    }

    /**
     * Save multiple courses
     */
    public void saveCourses(List<Course> courses) {
        // Group courses by batch
        Map<String, List<Course>> coursesByBatch = courses.stream()
                .filter(c -> c.getBatchId() != null)
                .collect(Collectors.groupingBy(Course::getBatchId));
        
        List<Batch> batches = loadBatches();
        int updatedCount = 0;
        
        for (Batch batch : batches) {
            List<Course> batchCoursesToUpdate = coursesByBatch.get(batch.getId());
            if (batchCoursesToUpdate != null && !batchCoursesToUpdate.isEmpty()) {
                List<Course> updatedCourses = new ArrayList<>(batch.getCourses());
                
                for (Course updatedCourse : batchCoursesToUpdate) {
                    for (int i = 0; i < updatedCourses.size(); i++) {
                        Course c = updatedCourses.get(i);
                        if (c.getId().equals(updatedCourse.getId())) {
                            updatedCourses.set(i, updatedCourse);
                            updatedCount++;
                            break;
                        }
                    }
                }
                batch.setCourses(updatedCourses);
            }
        }
        
        if (updatedCount > 0) {
            saveBatches(batches);
            System.out.println("✅ " + updatedCount + " courses updated");
        }
    }

    /**
     * Get courses for a specific department
     */
    public List<Course> getCoursesForDepartment(String departmentId) {
        return loadCourses().stream()
                .filter(c -> departmentId.equals(c.getDepartmentId()))
                .collect(Collectors.toList());
    }

    /**
     * Get courses for a specific batch
     */
    public List<Course> getCoursesForBatch(String batchId) {
        return loadCourses().stream()
                .filter(c -> batchId.equals(c.getBatchId()))
                .collect(Collectors.toList());
    }

    /**
     * Debug method to check course loading
     */
    public void debugCourses() {
        System.out.println("\n=== DEBUG: Course Loading ===");
        List<Batch> batches = loadBatches();
        System.out.println("Total batches: " + batches.size());
        
        int totalCourses = 0;
        for (Batch batch : batches) {
            List<Course> courses = batch.getCourses();
            if (courses != null) {
                System.out.println("Batch " + batch.getName() + " (" + batch.getId() + "): " + courses.size() + " courses");
                for (Course c : courses) {
                    System.out.println("  - " + c.getId() + ": " + c.getTitle() + " (" + c.getDepartmentId() + ")");
                }
                totalCourses += courses.size();
            } else {
                System.out.println("Batch " + batch.getName() + " has null courses!");
            }
        }
        System.out.println("Total courses across all batches: " + totalCourses);
        System.out.println("================================\n");
    }

    // ---- Batches ----
    public List<Batch> loadBatches() {
        return storage.getAllBatches();
    }

    public void saveBatches(List<Batch> batches) {
        var s = storage.getStore();
        s.getBatches().clear();
        if (batches != null) s.getBatches().addAll(batches);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    public void saveBatches(ObservableList<Batch> batches) {
        saveBatches(new ArrayList<>(batches));
    }

    // ---- Teachers ----
    public List<Teacher> loadTeachers() {
        return Collections.unmodifiableList(storage.getStore().getTeachers());
    }

    public void saveTeachers(List<Teacher> teachers) {
        var s = storage.getStore();
        s.getTeachers().clear();
        if (teachers != null) s.getTeachers().addAll(teachers);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    public void saveTeachers(ObservableList<Teacher> teachers) {
        saveTeachers(new ArrayList<>(teachers));
    }

    // ---- Rooms ----
    public List<Room> loadRooms() {
        return Collections.unmodifiableList(storage.getStore().getRooms());
    }

    public void saveRooms(List<Room> rooms) {
        var s = storage.getStore();
        s.getRooms().clear();
        if (rooms != null) s.getRooms().addAll(rooms);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    public void saveRooms(ObservableList<Room> rooms) {
        saveRooms(new ArrayList<>(rooms));
    }

    // ---- Merged Classes ----
    public List<MergedClassOption> loadMergedOptions() {
        return Collections.unmodifiableList(storage.getStore().getMergedOptions());
    }

    public void saveMergedOptions(List<MergedClassOption> opts) {
        var s = storage.getStore();
        s.getMergedOptions().clear();
        if (opts != null) s.getMergedOptions().addAll(opts);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    public void saveMergedOptions(ObservableList<MergedClassOption> opts) {
        saveMergedOptions(new ArrayList<>(opts));
    }

    // ---- Routine entries ----
    public List<RoutineEntry> loadRoutine() {
        return Collections.unmodifiableList(storage.getStore().getRoutine());
    }

    public void saveRoutine(List<RoutineEntry> routine) {
        var s = storage.getStore();
        s.getRoutine().clear();
        if (routine != null) s.getRoutine().addAll(routine);
        storage.save();
        saveAllToJson(); // Auto-save after changes
    }

    // ---- Compatibility for TeacherManageView ----
    public List<RoutineEntry> loadRoutines() {
        return new ArrayList<>(loadRoutine());
    }

    public boolean exportTeacherRoutine(String teacherId) {
        if (teacherId == null || teacherId.isEmpty()) return false;

        List<RoutineEntry> routines = loadRoutine();
        List<RoutineEntry> teacherRoutines = new ArrayList<>();

        for (RoutineEntry r : routines) {
            if (teacherId.equals(r.getTeacherId())) {
                teacherRoutines.add(r);
            }
        }

        if (teacherRoutines.isEmpty()) return false;

        System.out.println("Exporting " + teacherRoutines.size() + " routines for teacher " + teacherId);
        for (RoutineEntry r : teacherRoutines) {
            System.out.printf("%s | %s | %s | %s | %s%n",
                    r.getDay(), r.getTimeSlot(),
                    r.getCourseCode(), r.getTeacherName(),
                    r.getRoomNo() != null ? r.getRoomNo() : "—");
        }
        return true;
    }

    // ---- Exam routines ----
    public void saveExamRoutine(String batchId, List<Exam> exams) {
        storage.saveExamRoutine(batchId, exams);
        saveAllToJson(); // Auto-save after changes
    }

    public List<Exam> loadExamRoutine(String batchId) {
        return storage.getExamRoutine(batchId);
    }

    // ---- Expose underlying StorageService if needed ----
    public StorageService getStorageService() {
        return storage;
    }
}