package com.scheduler.model;

import java.time.LocalDate;

public class Exam {
    private String courseId;
    private LocalDate date;
    private String roomNo;  // Move this BEFORE the methods that use it

    public Exam() {}

    public Exam(String courseId, LocalDate date) {
        this.courseId = courseId;
        this.date = date;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String id) {
        this.courseId = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate d) {
        this.date = d;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
}