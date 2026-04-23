package com.scheduler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Department implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String code;
    private String name;
    private List<String> courseIds = new ArrayList<>();
    private List<String> teacherIds = new ArrayList<>();
    private List<String> batchIds = new ArrayList<>();

    // Transient fields for UI (not persisted)
    private transient List<Course> courses = new ArrayList<>();
    private transient List<Teacher> teachers = new ArrayList<>();
    private transient List<Batch> batches = new ArrayList<>();

    // Default constructor (required for JSON deserialization)
    public Department() {}

    // Constructor with id and name (for backward compatibility)
    public Department(String id, String name) {
        this.id = id;
        this.name = name;
        this.code = id; // Set code to id by default
    }

    // Full constructor
    public Department(String id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getCourseIds() { return courseIds; }
    public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds != null ? courseIds : new ArrayList<>(); }

    public List<String> getTeacherIds() { return teacherIds; }
    public void setTeacherIds(List<String> teacherIds) { this.teacherIds = teacherIds != null ? teacherIds : new ArrayList<>(); }

    public List<String> getBatchIds() { return batchIds; }
    public void setBatchIds(List<String> batchIds) { this.batchIds = batchIds != null ? batchIds : new ArrayList<>(); }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses != null ? courses : new ArrayList<>(); }

    public List<Teacher> getTeachers() { return teachers; }
    public void setTeachers(List<Teacher> teachers) { this.teachers = teachers != null ? teachers : new ArrayList<>(); }

    public List<Batch> getBatches() { return batches; }
    public void setBatches(List<Batch> batches) { this.batches = batches != null ? batches : new ArrayList<>(); }

    @Override
    public String toString() {
        return name != null ? name : id;
    }
}