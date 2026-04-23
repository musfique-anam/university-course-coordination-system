package com.scheduler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Batch is the heart of the system. Courses are created when Batch is created.
 */
public class Batch implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;           // e.g. "58th"
    private String departmentId;
    private ProgramType programType;
    private int totalStudents;

    // Store only course IDs for lightweight serialization
    private List<String> courseIds = new ArrayList<>();

    // Full course objects (used in views, not always persisted)
    private List<Course> courses = new ArrayList<>();

    public Batch() { }

    public Batch(String id, String name, List<String> courseIds) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.courseIds = courseIds != null ? courseIds : new ArrayList<>();
    }

    // -------------------- Basic info --------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public ProgramType getProgramType() { return programType; }
    public void setProgramType(ProgramType programType) { this.programType = programType; }

    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    // -------------------- Courses --------------------
    public List<String> getCourseIds() { return courseIds; }
    public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds != null ? courseIds : new ArrayList<>(); }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses != null ? courses : new ArrayList<>(); }

    // -------------------- Helper --------------------
    /**
     * Get a course by ID from this batch
     */
    public Course getCourseById(String courseId) {
        if (courses == null || courseId == null) return null;
        return courses.stream()
                .filter(c -> courseId.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }
}
