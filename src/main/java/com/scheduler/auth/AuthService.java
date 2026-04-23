package com.scheduler.auth;

import com.scheduler.model.Teacher;
import com.scheduler.storage.FileStorage;

import java.util.List;
import java.util.Optional;

/**
 * Admin: hardcoded.
 * Teacher: file-based login (account created by Admin).
 */
public class AuthService {

    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final FileStorage storage;
    private String currentUserId;
    private boolean isAdmin;

    public AuthService(FileStorage storage) {
        this.storage = storage;
        
        // Add shutdown hook for auto-save on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 Application shutting down - auto-saving data...");
            storage.saveAllToJson();
            System.out.println("✅ Data saved successfully");
        }));
    }

    public boolean loginAdmin(String id, String password) {
        if (ADMIN_ID.equals(id) && ADMIN_PASSWORD.equals(password)) {
            currentUserId = ADMIN_ID;
            isAdmin = true;
            return true;
        }
        return false;
    }

    public boolean loginTeacher(String teacherId, String password) {
        List<Teacher> teachers = storage.loadTeachers();

        Optional<Teacher> found = teachers.stream()
                .filter(t -> teacherId.equals(t.getTeacherId()))
                .findFirst();

        if (found.isPresent()) {
            String storedHash = found.get().getPasswordHash();
            String inputHash = simpleHash(password);

            if (storedHash != null && storedHash.equals(inputHash)) {
                currentUserId = teacherId;
                isAdmin = false;
                return true;
            }
        }

        return false;
    }

    public void logout() {
        // Save all data before logout
        storage.saveAllToJson();
        System.out.println("✅ Data saved before logout");
        
        currentUserId = null;
        isAdmin = false;
    }

    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    /** Simple hash for demo purposes. */
    public static String simpleHash(String password) {
        if (password == null) return "";
        return String.valueOf(password.hashCode());
    }
}