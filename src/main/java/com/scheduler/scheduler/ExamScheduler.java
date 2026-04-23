package com.scheduler.scheduler;

import com.scheduler.model.Course;
import com.scheduler.model.Exam;
import com.scheduler.storage.StorageService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExamScheduler {
    private final StorageService storage;

    public ExamScheduler(StorageService storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    /**
     * Schedule exams for a batch on alternate days between startDate and endDate.
     */
    public List<Exam> scheduleAlternateDays(String batchId, LocalDate startDate, LocalDate endDate, String roomId) {
        if (batchId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("batchId, startDate and endDate must be non-null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }

        // Filter courses for this batch safely
        List<Course> courses = storage.getAllCourses().stream()
                .filter(c -> Objects.equals(batchId, c.getBatchId()))
                .collect(Collectors.toList());

        if (courses.isEmpty()) {
            throw new IllegalArgumentException("No courses found for batch: " + batchId);
        }

        // Generate alternate dates
        List<LocalDate> altDates = new ArrayList<>();
        LocalDate d = startDate;
        boolean take = true;
        while (!d.isAfter(endDate)) {
            if (take) altDates.add(d);
            take = !take;
            d = d.plusDays(1);
        }

        if (altDates.size() < courses.size()) {
            throw new IllegalArgumentException("Not enough alternate days to schedule all courses for batch: " + batchId);
        }

        // Create exams
        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            Exam ex = new Exam(courses.get(i).getId(), altDates.get(i));
            ex.setRoomNo(roomId); // assign room if provided
            exams.add(ex);
        }

        // Save to storage
        storage.saveExamRoutine(batchId, exams);
        return exams;
    }
}
