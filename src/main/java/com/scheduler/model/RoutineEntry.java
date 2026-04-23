package com.scheduler.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * One row in the routine: day, time, course, teacher, room, batch(es).
 * For merged class: multiple batchIds.
 */
public class RoutineEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String batchId;     // optional
    private String departmentId; // optional
    private String courseId;
    private String courseCode;
    private String courseTitle;
    private LocalDate date;
    private LocalTime time;
    private String roomId;      // assigned room id/number
    private boolean labSession; // true if lab

    // convenience fields used by views/services
    private String teacherId;
    private String teacherName;
    private List<String> batchIds = new ArrayList<>();
    private boolean merged = false;

    public RoutineEntry() {}


    // Add these methods to your RoutineEntry.java
public void setDay(String day) {
    // Convert day string to LocalDate and set
    java.time.LocalDate date = java.time.LocalDate.now();
    java.time.DayOfWeek targetDay = convertToDayOfWeek(day);
    while (date.getDayOfWeek() != targetDay) {
        date = date.plusDays(1);
    }
    this.date = date;
}

public void setTimeSlot(String timeSlot) {
    this.time = convertToLocalTime(timeSlot);
}

public void setCourseCodes(List<String> courseCodes) {
    if (courseCodes != null && !courseCodes.isEmpty()) {
        this.courseId = courseCodes.get(0);
        this.courseCode = courseCodes.get(0);
    }
}

private java.time.DayOfWeek convertToDayOfWeek(String day) {
    switch (day) {
        case "Saturday": return java.time.DayOfWeek.SATURDAY;
        case "Sunday": return java.time.DayOfWeek.SUNDAY;
        case "Monday": return java.time.DayOfWeek.MONDAY;
        case "Tuesday": return java.time.DayOfWeek.TUESDAY;
        case "Wednesday": return java.time.DayOfWeek.WEDNESDAY;
        case "Thursday": return java.time.DayOfWeek.THURSDAY;
        case "Friday": return java.time.DayOfWeek.FRIDAY;
        default: return java.time.DayOfWeek.MONDAY;
    }
}

private java.time.LocalTime convertToLocalTime(String timeSlot) {
    switch (timeSlot) {
        case "8:30-9:50": return java.time.LocalTime.of(8, 30);
        case "10:00-11:20": return java.time.LocalTime.of(10, 0);
        case "11:30-12:50": return java.time.LocalTime.of(11, 30);
        case "1:30-2:50": return java.time.LocalTime.of(13, 30);
        case "3:00-4:20": return java.time.LocalTime.of(15, 0);
        case "4:30-5:50": return java.time.LocalTime.of(16, 30);
        default: return java.time.LocalTime.of(9, 0);
    }
}
    // teacher helpers
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName != null ? teacherName : ""; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    // batch helpers (support legacy single batchId field and newer list)
    public List<String> getBatchIds() {
        if (batchIds != null && !batchIds.isEmpty()) return batchIds;
        return batchId != null ? List.of(batchId) : Collections.emptyList();
    }
    public void setBatchIds(List<String> batchIds) { this.batchIds = batchIds != null ? batchIds : new ArrayList<>(); }
    public List<String> getBatchNames() { return getBatchIds(); }

    // merged flag for UI
    public boolean isMerged() { return merged; }
    public void setMerged(boolean merged) { this.merged = merged; }

    // day / time helpers expected by views
    public String getDay() {
        if (date == null) return "";
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
    public String getTimeSlot() {
        if (time == null) return "";
        return time.toString();
    }

    // room helper (roomNo used throughout views)
    public String getRoomNo() { return roomId; }
    public void setRoomNo(String roomNo) { this.roomId = roomNo; }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public boolean isLabSession() { return labSession; }
    public void setLabSession(boolean labSession) { this.labSession = labSession; }
}
