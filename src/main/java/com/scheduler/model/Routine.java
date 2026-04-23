package com.scheduler.model;

import java.util.List;

public class Routine {
    private String day;
    private String timeSlot;
    private String courseCode;
    private String courseTitle;
    private String teacherId;
    private String teacherName;
    private String roomNo;
    private String departmentId;
    private List<String> batchIds;
    private List<String> batchNames;
    private boolean merged;

    public Routine() {}

    public Routine(String day, String timeSlot, String courseCode, String courseTitle,
                   String teacherId, String teacherName, String roomNo, String departmentId,
                   List<String> batchIds, List<String> batchNames, boolean merged) {
        this.day = day;
        this.timeSlot = timeSlot;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.roomNo = roomNo;
        this.departmentId = departmentId;
        this.batchIds = batchIds;
        this.batchNames = batchNames;
        this.merged = merged;
    }

    // Getters & setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public List<String> getBatchIds() { return batchIds; }
    public void setBatchIds(List<String> batchIds) { this.batchIds = batchIds; }

    public List<String> getBatchNames() { return batchNames; }
    public void setBatchNames(List<String> batchNames) { this.batchNames = batchNames; }

    public boolean isMerged() { return merged; }
    public void setMerged(boolean merged) { this.merged = merged; }
}
