package com.scheduler.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Teacher implements Serializable {
    private static final long serialVersionUID = 1L;

    private String teacherId;
    private String name;
    private String shortName;
    private String email;
    private String phone;
    private String departmentId;
    private int maxCreditLoad;
    private ProgramType programPreference;
    private List<String> interestedCourseCodes = new ArrayList<>();
    private List<String> availableDays = new ArrayList<>();
    private List<String> availableTimeSlots = new ArrayList<>();
    private String passwordHash;

    public Teacher() {}

    // ---------- ID ----------
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String t) { this.teacherId = t; }

    // 🔥 IMPORTANT: Needed for DataStore compatibility
    public String getId() { return teacherId; }

    // ---------- Basic Info ----------
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public String getShortName() {
        return shortName != null && !shortName.isEmpty()
                ? shortName
                : (name != null && name.length() >= 2
                   ? name.substring(0, 2).toUpperCase()
                   : teacherId);
    }
    public void setShortName(String s) { this.shortName = s; }

    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }

    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String d) { this.departmentId = d; }

    public int getMaxCreditLoad() { return maxCreditLoad; }
    public void setMaxCreditLoad(int m) { this.maxCreditLoad = m; }

    public ProgramType getProgramPreference() { return programPreference; }
    public void setProgramPreference(ProgramType p) { this.programPreference = p; }

    public List<String> getInterestedCourseCodes() { return interestedCourseCodes; }
    public void setInterestedCourseCodes(List<String> l) {
        this.interestedCourseCodes = l != null ? l : new ArrayList<>();
    }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> l) {
        this.availableDays = l != null ? l : new ArrayList<>();
    }

    public List<String> getAvailableTimeSlots() { return availableTimeSlots; }
    public void setAvailableTimeSlots(List<String> l) {
        this.availableTimeSlots = l != null ? l : new ArrayList<>();
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
}
