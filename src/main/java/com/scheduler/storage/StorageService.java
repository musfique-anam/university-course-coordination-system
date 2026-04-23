package com.scheduler.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scheduler.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages persistence of DataStore to JSON file
 */
public class StorageService {

    private final Path dataFile;
    private final ObjectMapper mapper;
    private DataStore store;

   public StorageService() {
    String home = System.getProperty("user.home");
    Path dir = Paths.get(home, ".smart-scheduler");
    this.dataFile = dir.resolve("data.json");
    this.mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ensureDir(dir);
    load();
}

    private void ensureDir(Path dir) {
        try { Files.createDirectories(dir); } catch (Exception ignored) {}
    }

    public synchronized void load() {
        try {
            if (Files.exists(dataFile) && Files.size(dataFile) > 0) {
                store = mapper.readValue(dataFile.toFile(), DataStore.class);
            } else {
                store = new DataStore();
                save();
            }
        } catch (Exception e) {
            store = new DataStore();
        }
    }

    public synchronized void save() {
        try {
            Path tmp = Files.createTempFile(dataFile.getParent(), "data", ".json.tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), store);
            Files.move(tmp, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== NEW METHODS FOR JSON FILE OPERATIONS ==========
    
    /**
     * Load data from a specific JSON file (for import)
     */
    public synchronized void loadFromFile(File file) throws IOException {
        if (file != null && file.exists()) {
            DataStore importedStore = mapper.readValue(file, DataStore.class);
            if (importedStore != null) {
                this.store = importedStore;
                save(); // Save to default location after import
            }
        }
    }

    /**
     * Save data to a specific JSON file (for export)
     */
    public synchronized void saveToFile(File file) throws IOException {
        if (file != null) {
            // Ensure parent directories exist
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, store);
        }
    }

    // --- Departments ---
    public List<Department> getAllDepartments() { 
        return Collections.unmodifiableList(store.getDepartments()); 
    }
    
    public void addDepartment(Department d) { 
        if (d != null) { 
            store.getDepartments().add(d); 
            save(); 
        } 
    }
    
    public Department getDepartmentById(String id) { 
        return store.getDepartmentById(id); 
    }

    // --- Courses ---
    public List<Course> getAllCourses() { 
        return Collections.unmodifiableList(store.getCourses()); 
    }
    
    public void addCourse(Course c) { 
        if (c != null) { 
            store.getCourses().add(c); 
            save(); 
        } 
    }
    
    public List<Course> getCoursesForDepartment(String departmentId) { 
        return store.getCoursesForDepartment(departmentId); 
    }
    
    public List<Course> getCoursesForBatch(String batchId) { 
        return store.getCoursesForBatch(batchId); 
    }
    
    public Map<String, List<Course>> getCoursesGroupedByDepartment() { 
        return store.getCoursesGroupedByDepartment(); 
    }

    // --- Batches ---
    public List<Batch> getAllBatches() { 
        return Collections.unmodifiableList(store.getBatches()); 
    }
    
    public void addBatch(Batch b) { 
        if (b != null) { 
            store.getBatches().add(b); 
            save(); 
        } 
    }
    
    public Batch getBatchById(String id) { 
        return store.getBatchById(id); 
    }

    // --- Teachers ---
    public List<Teacher> getAllTeachers() { 
        return Collections.unmodifiableList(store.getTeachers()); 
    }
    
    public void saveTeachers(List<Teacher> teachers) { 
        store.getTeachers().clear(); 
        if (teachers != null) store.getTeachers().addAll(teachers); 
        save(); 
    }
    
    public Teacher getTeacherById(String id) { 
        return store.getTeacherById(id); 
    }

    // --- Rooms ---
    public List<Room> getAllRooms() { 
        return Collections.unmodifiableList(store.getRooms()); 
    }
    
    public void saveRooms(List<Room> rooms) { 
        store.getRooms().clear(); 
        if (rooms != null) store.getRooms().addAll(rooms); 
        save(); 
    }
    
    public Room getRoomById(String id) { 
        return store.getRoomById(id); 
    }

    // --- Routine ---
    public List<RoutineEntry> getRoutine() { 
        return Collections.unmodifiableList(store.getRoutine()); 
    }
    
    public void saveRoutine(List<RoutineEntry> routine) { 
        store.getRoutine().clear(); 
        if (routine != null) store.getRoutine().addAll(routine); 
        save(); 
    }

    // --- Merged Classes ---
    public List<MergedClassOption> getMergedOptions() { 
        return Collections.unmodifiableList(store.getMergedOptions()); 
    }
    
    public void saveMergedOptions(List<MergedClassOption> opts) {
        store.getMergedOptions().clear();
        if (opts != null) store.getMergedOptions().addAll(opts);
        save();
    }

    // --- Exam Routines ---
    public void saveExamRoutine(String key, List<Exam> exams) {
        if (key != null && exams != null) { 
            store.saveExamRoutine(key, exams); 
            save(); 
        }
    }

    public List<Exam> getExamRoutine(String key) {
        return store.getExamRoutine(key);
    }

    // --- Access ---
    public DataStore getStore() { 
        return store; 
    }
}