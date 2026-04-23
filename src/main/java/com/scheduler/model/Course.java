package com.scheduler.model;

import java.io.Serializable;

/**
 * Course entity
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;              // Unique ID (ex: CSE101)
    private String title;           // Course name
    private int credit;
    private CourseType courseType;
    private String departmentId;
    private ProgramType programType;
    private String batchId;

    public Course() {}

    public Course(String id, String title, int credit,
                  CourseType courseType,
                  String departmentId,
                  ProgramType programType,
                  String batchId) {
        this.id = id;
        this.title = title;
        this.credit = credit;
        this.courseType = courseType;
        this.departmentId = departmentId;
        this.programType = programType;
        this.batchId = batchId;
    }

    // ---------- Business Logic ----------
    public int getClassesPerWeek() {
        if (courseType == CourseType.THEORY) {
            return credit; // 3 credit = 3 classes
        } else {
            return 1; // Lab = 1 session
        }
    }

    public boolean isLab() {
        return courseType == CourseType.LAB;
    }

    // ---------- Getters & Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCredit() { return credit; }
    public void setCredit(int credit) { this.credit = credit; }

    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public ProgramType getProgramType() { return programType; }
    public void setProgramType(ProgramType programType) { this.programType = programType; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
}
