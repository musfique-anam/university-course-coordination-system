package com.scheduler.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scheduler.model.*;
import com.scheduler.storage.StorageService;

import java.io.File;
import java.util.List;

public class DataSeeder {  // <-- changed class name to match file

    /**
     * Seeds the StorageService database from a JSON file.
     * JSON structure should have "rooms" and "departments" lists.
     * Each department can have courses, batches, and teachers.
     */
    public static void seedFromJson(StorageService storage, String jsonPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            // Read JSON into DataSeed helper class
            DataSeedHelper dataSeed = mapper.readValue(new File(jsonPath), DataSeedHelper.class);

            // -------------------- Rooms --------------------
            if (dataSeed.rooms != null) {
                storage.saveRooms(dataSeed.rooms);
            }

            // -------------------- Departments --------------------
            if (dataSeed.departments != null) {
                for (Department d : dataSeed.departments) {

                    // Save department itself
                    storage.getStore().getDepartments().add(d);
                    storage.save();  // persist changes

                    // Save courses of this department
                    if (d.getCourses() != null) {
                        for (Course c : d.getCourses()) {
                            storage.addCourse(c);
                        }
                    }

                    // Save batches of this department
                    if (d.getBatches() != null) {
                        for (Batch b : d.getBatches()) {
                            storage.addBatch(b);
                        }
                    }

                    // Save teachers of this department
                    if (d.getTeachers() != null) {
                        for (Teacher t : d.getTeachers()) {
                            storage.getStore().getTeachers().add(t);
                        }
                    }

                    storage.save();  // save after each department
                }
            }

            System.out.println("Database seeded from JSON successfully!");

        } catch (Exception e) {
            System.err.println("Failed to seed from JSON:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StorageService storage = new StorageService();
        seedFromJson(storage, "src/main/resources/data_seed.json"); // path to your JSON
    }

    // -------------------- Helper class for JSON --------------------
    static class DataSeedHelper {
        public List<Department> departments;
        public List<Room> rooms;
    }
}
