package com.scheduler.service;

import com.scheduler.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conflict detection: teacher clash, time clash, room clash, credit overflow, capacity mismatch.
 */
public class ConflictChecker {

    public boolean hasConflict(RoutineEntry newEntry, List<RoutineEntry> existing,
                              Map<String, Integer> teacherCreditUsed,
                              Map<String, Set<String>> teacherTimeUsed,
                              Map<String, Set<String>> roomTimeUsed,
                              Map<String, Set<String>> batchTimeUsed,
                              Course course, Teacher teacher, Batch batch, Room room) {
        String daySlot = newEntry.getDay() + "|" + newEntry.getTimeSlot();
        if (teacherTimeUsed.getOrDefault(teacher.getTeacherId(), Set.of()).contains(daySlot)) return true;
        if (roomTimeUsed.getOrDefault(room.getRoomNo(), Set.of()).contains(daySlot)) return true;
        for (String bid : newEntry.getBatchIds()) {
            if (batchTimeUsed.getOrDefault(bid, Set.of()).contains(daySlot)) return true;
        }
        int creditAfter = teacherCreditUsed.getOrDefault(teacher.getTeacherId(), 0) + course.getCredit();
        if (creditAfter > teacher.getMaxCreditLoad()) return true;
        if (room.getCapacity() < batch.getTotalStudents()) return true;
        return false;
    }

    public boolean validateFullRoutine(List<RoutineEntry> routine, List<Teacher> teachers, List<Batch> batches) {
        for (RoutineEntry e : routine) {
            if (e.getTeacherId() == null || e.getRoomNo() == null || e.getDay() == null || e.getTimeSlot() == null)
                return false;
        }
        return true;
    }
}
