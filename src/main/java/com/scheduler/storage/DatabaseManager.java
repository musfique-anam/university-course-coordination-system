package com.scheduler.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // This will create a file named 'scheduler.db' in your project root
    private static final String URL = "jdbc:sqlite:scheduler.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // 1. Create Departments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS departments (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL)");

            // 2. Create Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_no TEXT PRIMARY KEY, " +
                    "floor INTEGER, " +
                    "capacity INTEGER, " +
                    "room_type TEXT, " +
                    "total_pcs INTEGER, " +
                    "has_projector BOOLEAN)");

            // 3. Create Teachers Table
            stmt.execute("CREATE TABLE IF NOT EXISTS teachers (" +
                    "teacher_id TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "department_id TEXT, " +
                    "max_credit_load INTEGER)");

            // 4. Create Routine Entry Table (The Generated Schedule)
            stmt.execute("CREATE TABLE IF NOT EXISTS routine (" +
                    "id TEXT PRIMARY KEY, " +
                    "batch_id TEXT, " +
                    "course_id TEXT, " +
                    "teacher_id TEXT, " +
                    "room_id TEXT, " +
                    "day_of_week TEXT, " +
                    "time_slot TEXT)");

            System.out.println("✅ SQLite Database and Tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
        }
    }
}