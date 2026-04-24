package com.scheduler.auth;

import com.scheduler.model.Teacher;
import com.scheduler.storage.DatabaseStorage;

import java.util.List;
import java.util.Optional;

/**
 * Admin: hardcoded.
 * Teacher: database-based login (account created by Admin).
 */
public class AuthService {

    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final DatabaseStorage storage;
    private String currentUserId;
    private boolean isAdmin;

    public AuthService(DatabaseStorage storage) {
        this.storage = storage;
        
        /* * Note: The shutdown hook for auto-saving has been removed.
         * By migrating to SQLite, data is written instantly to the disk 
         * during operations. We no longer need to serialize the entire 
         * state of the application into a JSON file when the app closes.
         */
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
        // This now queries the SQLite database
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
        System.out.println("✅ User logged out securely.");
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