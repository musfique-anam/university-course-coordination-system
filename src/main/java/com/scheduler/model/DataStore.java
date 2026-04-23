package com.scheduler.model;

import java.io.Serializable;
import java.util.*;

/**
 * Main in-memory data holder
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    // Keep fields private, provide getters and setters
    private final List<Department> departments = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<Batch> batches = new ArrayList<>();
    private final List<Teacher> teachers = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<RoutineEntry> routine = new ArrayList<>();
    private final List<MergedClassOption> mergedOptions = new ArrayList<>();
    private final Map<String, List<Exam>> examRoutines = new HashMap<>();

    // ---------- Getters ----------
    public List<Department> getDepartments() { return departments; }
    public List<Course> getCourses() { return courses; }
    public List<Batch> getBatches() { return batches; }
    public List<Teacher> getTeachers() { return teachers; }
    public List<Room> getRooms() { return rooms; }
    public List<RoutineEntry> getRoutine() { return routine; }
    public List<MergedClassOption> getMergedOptions() { return mergedOptions; }
    public Map<String, List<Exam>> getExamRoutines() { return examRoutines; }

    // ---------- Helpers ----------
    public Department getDepartmentById(String id) {
        if (id == null) return null;
        return departments.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }

    public Course getCourseById(String id) {
        if (id == null) return null;
        return courses.stream().filter(c -> id.equals(c.getId())).findFirst().orElse(null);
    }

    public Batch getBatchById(String id) {
        if (id == null) return null;
        return batches.stream().filter(b -> id.equals(b.getId())).findFirst().orElse(null);
    }

    public Teacher getTeacherById(String id) {
        if (id == null) return null;
        return teachers.stream().filter(t -> id.equals(t.getTeacherId())).findFirst().orElse(null);
    }

    public Room getRoomById(String id) {
        if (id == null) return null;
        return rooms.stream().filter(r -> id.equals(r.getRoomNo())).findFirst().orElse(null);
    }

    public List<Course> getCoursesForDepartment(String departmentId) {
        if (departmentId == null) return Collections.emptyList();
        List<Course> list = new ArrayList<>();
        for (Course c : courses) {
            if (departmentId.equals(c.getDepartmentId())) list.add(c);
        }
        return list;
    }

    public List<Course> getCoursesForBatch(String batchId) {
        if (batchId == null) return Collections.emptyList();
        List<Course> list = new ArrayList<>();
        for (Course c : courses) {
            if (batchId.equals(c.getBatchId())) list.add(c);
        }
        return list;
    }

    public Map<String, List<Course>> getCoursesGroupedByDepartment() {
        Map<String, List<Course>> map = new HashMap<>();
        for (Course c : courses) {
            map.computeIfAbsent(c.getDepartmentId(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    // ---------- Exam Routine Helpers ----------
    public void saveExamRoutine(String key, List<Exam> exams) {
        examRoutines.put(key, exams);
    }

    public List<Exam> getExamRoutine(String key) {
        return examRoutines.getOrDefault(key, Collections.emptyList());
    }
}
