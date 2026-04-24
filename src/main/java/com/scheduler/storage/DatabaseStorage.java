package com.scheduler.storage;

import com.scheduler.model.Batch;
import com.scheduler.model.Course;
import com.scheduler.model.Department;
import com.scheduler.model.Room;
import com.scheduler.model.RoomType;
import com.scheduler.model.RoutineEntry;
import com.scheduler.model.Teacher;
import com.scheduler.model.MergedClassOption;
import com.scheduler.model.Exam;

import java.io.File;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStorage {

    public DatabaseStorage() {
        DatabaseManager.initializeDatabase();
    }

    // --- DEPARTMENTS ---
    public void saveDepartments(List<Department> departments) {
        String sql = "INSERT OR REPLACE INTO departments (id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Department dept : departments) {
                pstmt.setString(1, dept.getId());
                pstmt.setString(2, dept.getName());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) { System.err.println("Error saving departments: " + e.getMessage()); }
    }

    public List<Department> loadDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT id, name FROM departments";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                departments.add(new Department(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) { System.err.println("Error loading departments: " + e.getMessage()); }
        return departments;
    }

    // --- ROOMS ---
    public void saveRooms(List<Room> rooms) {
        String sql = "INSERT OR REPLACE INTO rooms (room_no, floor, capacity, room_type, total_pcs, has_projector) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Room room : rooms) {
                pstmt.setString(1, room.getRoomNo());
                pstmt.setInt(2, room.getFloor());
                pstmt.setInt(3, room.getCapacity());
                pstmt.setString(4, room.getRoomType() != null ? room.getRoomType().name() : RoomType.THEORY.name());
                pstmt.setInt(5, room.getTotalPCs());
                pstmt.setBoolean(6, room.isHasProjector());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) { System.err.println("Error saving rooms: " + e.getMessage()); }
    }

    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Room r = new Room();
                r.setRoomNo(rs.getString("room_no"));
                r.setFloor(rs.getInt("floor"));
                r.setCapacity(rs.getInt("capacity"));
                r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
                r.setTotalPCs(rs.getInt("total_pcs"));
                r.setHasProjector(rs.getBoolean("has_projector"));
                r.setAvailable(true);
                rooms.add(r);
            }
        } catch (SQLException e) { System.err.println("Error loading rooms: " + e.getMessage()); }
        return rooms;
    }

    // --- TEACHERS ---
    public void saveTeachers(List<Teacher> teachers) {
        System.out.println("Notice: saveTeachers SQL not yet implemented.");
    }

    public List<Teacher> loadTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Teacher t = new Teacher();
                t.setTeacherId(rs.getString("teacher_id"));
                t.setName(rs.getString("name"));
                t.setDepartmentId(rs.getString("department_id"));
                t.setMaxCreditLoad(rs.getInt("max_credit_load"));
                teachers.add(t);
            }
        } catch (SQLException e) { System.err.println("Error loading teachers: " + e.getMessage()); }
        return teachers;
    }

    // ==========================================
    // MISSING STUB METHODS ADDED FOR COMPILATION
    // ==========================================

    public List<Course> loadCourses() { return new ArrayList<>(); }
    public void saveCourses(List<Course> courses) { System.out.println("Notice: saveCourses SQL not yet implemented."); }

    public List<Batch> loadBatches() { return new ArrayList<>(); }
    public void saveBatches(List<Batch> batches) { System.out.println("Notice: saveBatches SQL not yet implemented."); }

    public List<RoutineEntry> loadRoutine() { return new ArrayList<>(); }
    public void saveRoutine(List<RoutineEntry> routine) { System.out.println("Notice: saveRoutine SQL not yet implemented."); }

    // --- NEW STUBS FOR MERGE, EXAMS, AND EXPORTS ---
    public List<MergedClassOption> loadMergedOptions() { return new ArrayList<>(); }
    public void saveMergedOptions(List<MergedClassOption> options) { System.out.println("Notice: saveMergedOptions SQL not yet implemented."); }
    
    public void saveExamRoutine(String batchId, List<Exam> exams) { System.out.println("Notice: saveExamRoutine SQL not yet implemented."); }
    public List<Exam> loadExamRoutine(String batchId) { return new ArrayList<>(); } // <-- MISSING METHOD ADDED HERE
    
    public boolean exportTeacherRoutine(String teacherId) { 
        System.out.println("Notice: exportTeacherRoutine not yet implemented."); 
        return false; 
    }

    // This satisfies the RoutineGenerator's old dependency structure
    public StorageService getStorageService() { return null; }

    // --- LEGACY JSON METHODS ---
    public File chooseJsonFile(Stage stage) { return null; }
    public boolean loadFromJson(File file) { return false; }
}