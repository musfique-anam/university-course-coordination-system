package com.scheduler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scheduler.model.*;
import com.scheduler.storage.StorageService;

import java.io.File;
import java.util.List;

/**
 * Seeds the StorageService database from a JSON file.
 * JSON structure should have "rooms" and "departments" lists.
 */
public class JsonDataSeeder {

    public static void seedFromJson(StorageService storage, String jsonPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            // Read JSON into helper class
            DataSeed dataSeed = mapper.readValue(new File(jsonPath), DataSeed.class);

            // -------------------- Rooms --------------------
            if (dataSeed.rooms != null) {
                storage.saveRooms(dataSeed.rooms);
            }

            // -------------------- Departments --------------------
            if (dataSeed.departments != null) {
                for (Department d : dataSeed.departments) {

                    // Save department itself
                    if (d != null) {
                        storage.getStore().getDepartments().add(d);

                        // -------------------- Courses --------------------
                        if (d.getCourses() != null) {
                            for (Course c : d.getCourses()) {
                                storage.getStore().getCourses().add(c);
                            }
                        }

                        // -------------------- Batches --------------------
                        if (d.getBatches() != null) {
                            for (Batch b : d.getBatches()) {
                                storage.getStore().getBatches().add(b);
                            }
                        }

                        // -------------------- Teachers --------------------
                        if (d.getTeachers() != null) {
                            for (Teacher t : d.getTeachers()) {
                                storage.getStore().getTeachers().add(t);
                            }
                        }
                    }
                }
            }

            // Persist all changes
            storage.save();
            System.out.println("Database seeded from JSON successfully!");

        } catch (Exception e) {
            System.err.println("Failed to seed from JSON:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StorageService storage = new StorageService();
        seedFromJson(storage, "src/main/resources/data_seed.json"); // Adjust JSON path
    }

    // -------------------- Helper class for JSON --------------------
    static class DataSeed {
        public List<Department> departments;
        public List<Room> rooms;
    }
}
