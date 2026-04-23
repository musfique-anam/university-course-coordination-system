package com.scheduler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin manually selects batches to merge: same teacher, room, time. Max 4 batches.
 */
public class MergedClassOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private List<String> batchIds = new ArrayList<>();
    private List<String> courseCodes = new ArrayList<>(); // may differ per batch
    private String teacherId;
    private String roomNo;
    private String timeSlot;
    private String day;

    public MergedClassOption() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<String> getBatchIds() { return batchIds; }
    public void setBatchIds(List<String> batchIds) { this.batchIds = batchIds != null ? batchIds : new ArrayList<>(); }
    public List<String> getCourseCodes() { return courseCodes; }
    public void setCourseCodes(List<String> courseCodes) { this.courseCodes = courseCodes != null ? courseCodes : new ArrayList<>(); }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
}
