package com.scheduler.service;

import com.scheduler.model.*;
import com.scheduler.storage.DatabaseStorage;
import com.scheduler.storage.StorageService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class RoutineGenerator {

    private final DatabaseStorage storage;
    private final StorageService sService;

    public RoutineGenerator(DatabaseStorage storage) {
        this.storage = storage;
        this.sService = storage.getStorageService();
    }

    // ---------------- Class Routine for Batch ----------------
    public List<RoutineEntry> generateClassRoutineForBatch(String batchId,
                                                           LocalDate startDate,
                                                           List<LocalTime> timeSlots,
                                                           Map<String, String> theoryRoomForCourse,
                                                           List<Room> theoryRoomPool,
                                                           List<Room> labRoomPool) {

        Objects.requireNonNull(batchId);
        if (startDate == null) startDate = LocalDate.now().plusDays(1);

        System.out.println("\n=== Generating routine for batch: " + batchId + " ===");
        
        // Get courses for this batch
        List<Course> courses = storage.loadCourses().stream()
                .filter(c -> batchId.equals(c.getBatchId()))
                .collect(Collectors.toList());

        System.out.println("Courses in this batch: " + courses.size());

        if (courses.isEmpty()) {
            System.out.println("❌ No courses found for batch: " + batchId);
            return Collections.emptyList();
        }

        // Get batch details
        Batch batch = storage.loadBatches().stream()
                .filter(b -> batchId.equals(b.getId()))
                .findFirst()
                .orElse(null);
        
        if (batch == null) {
            System.out.println("❌ Batch not found: " + batchId);
            return Collections.emptyList();
        }
        
        System.out.println("Batch department: " + batch.getDepartmentId());

        // Get teachers for this department
        List<Teacher> deptTeachers = storage.loadTeachers().stream()
                .filter(t -> batch.getDepartmentId().equals(t.getDepartmentId()))
                .collect(Collectors.toList());
        
        System.out.println("Teachers in department: " + deptTeachers.size());

        // Get available rooms
        List<String> theoryRooms = (theoryRoomPool == null || theoryRoomPool.isEmpty()) ? new ArrayList<>()
                : theoryRoomPool.stream().filter(Objects::nonNull).map(Room::getRoomNo).collect(Collectors.toList());
        List<String> labRooms = (labRoomPool == null || labRoomPool.isEmpty()) ? new ArrayList<>()
                : labRoomPool.stream().filter(Objects::nonNull).map(Room::getRoomNo).collect(Collectors.toList());

        System.out.println("Theory rooms: " + theoryRooms.size());
        System.out.println("Lab rooms: " + labRooms.size());

        // Create time slots if none provided
        if (timeSlots == null || timeSlots.isEmpty()) {
            timeSlots = Arrays.asList(
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                LocalTime.of(13, 30),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30)
            );
        }

        List<RoutineEntry> routine = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayIndex = 0;
        int slotIndex = 0;

        // Track used combinations to avoid conflicts
        Set<String> usedTeacherSlots = new HashSet<>();
        Set<String> usedRoomSlots = new HashSet<>();

        ProgramType batchProgram = batch.getProgramType();
        String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};

        for (Course course : courses) {
            System.out.println("\nAssigning course: " + course.getId());
            
            boolean assigned = false;
            int attempts = 0;
            int maxAttempts = 100;

            while (!assigned && attempts < maxAttempts) {
                attempts++;
                
                // Cycle through days and slots
                String day = days[dayIndex % days.length];
                LocalTime time = timeSlots.get(slotIndex % timeSlots.size());
                String timeSlot = time.toString();
                
                // Create unique keys
                String teacherSlotKey = day + "_" + timeSlot;
                String roomSlotKey = day + "_" + timeSlot;

                // Find eligible teachers for this course at this day/time
                List<Teacher> eligibleTeachers = deptTeachers.stream()
                        .filter(t -> t.getInterestedCourseCodes() != null && 
                                     t.getInterestedCourseCodes().contains(course.getId()))
                        .filter(t -> t.getProgramPreference() == batchProgram || t.getProgramPreference() == ProgramType.BOTH)
                        .filter(t -> t.getAvailableDays() != null && t.getAvailableDays().contains(day))
                        .filter(t -> t.getAvailableTimeSlots() != null && 
                                     t.getAvailableTimeSlots().contains(timeSlot))
                        .filter(t -> !usedTeacherSlots.contains(t.getTeacherId() + "_" + teacherSlotKey))
                        .collect(Collectors.toList());

                if (eligibleTeachers.isEmpty()) {
                    // Move to next slot
                    slotIndex++;
                    if (slotIndex >= timeSlots.size()) {
                        slotIndex = 0;
                        dayIndex++;
                    }
                    continue;
                }

                // Find available room
                String roomId = null;
                List<String> availableRooms = course.isLab() ? labRooms : theoryRooms;
                
                for (String room : availableRooms) {
                    if (!usedRoomSlots.contains(room + "_" + roomSlotKey)) {
                        roomId = room;
                        break;
                    }
                }

                if (roomId == null) {
                    // No room available, move to next slot
                    slotIndex++;
                    if (slotIndex >= timeSlots.size()) {
                        slotIndex = 0;
                        dayIndex++;
                    }
                    continue;
                }

                // Select a teacher (first eligible)
                Teacher selectedTeacher = eligibleTeachers.get(0);
                
                // Create routine entry
                RoutineEntry entry = new RoutineEntry();
                entry.setId(UUID.randomUUID().toString());
                entry.setBatchId(batchId);
                entry.setCourseId(course.getId());
                entry.setCourseCode(course.getId());
                entry.setCourseTitle(course.getTitle());
                entry.setLabSession(course.isLab());
                entry.setTeacherId(selectedTeacher.getTeacherId());
                entry.setTeacherName(selectedTeacher.getName());
                entry.setRoomId(roomId);
                entry.setDate(currentDate.plusDays(dayIndex));
                entry.setTime(time);
                
                // Mark as used
                usedTeacherSlots.add(selectedTeacher.getTeacherId() + "_" + teacherSlotKey);
                usedRoomSlots.add(roomId + "_" + roomSlotKey);
                
                routine.add(entry);
                assigned = true;
                
                System.out.println("  ✅ Assigned: " + course.getId() + 
                                 " -> Teacher: " + selectedTeacher.getName() + 
                                 ", Room: " + roomId + 
                                 ", Day: " + day + 
                                 ", Time: " + timeSlot);
                
                // Move to next slot for next course
                slotIndex++;
                if (slotIndex >= timeSlots.size()) {
                    slotIndex = 0;
                    dayIndex++;
                }
            }

            if (!assigned) {
                System.out.println("❌ Failed to assign course: " + course.getId() + " after " + maxAttempts + " attempts");
                return Collections.emptyList();
            }
        }

        storage.saveRoutine(routine);
        System.out.println("\n✅ Successfully generated " + routine.size() + " routine entries for batch " + batchId);
        return routine;
    }

    // ---------------- Class Routine for Department ----------------
    public List<RoutineEntry> generateClassRoutineForDepartment(String departmentId,
                                                                LocalDate startDate,
                                                                List<LocalTime> timeSlots,
                                                                Map<String, String> theoryRoomForCourse,
                                                                List<Room> theoryRoomPool,
                                                                List<Room> labRoomPool) {

        Objects.requireNonNull(departmentId);
        if (startDate == null) startDate = LocalDate.now().plusDays(1);

        System.out.println("\n=== Generating routine for department: " + departmentId + " ===");
        
        List<Course> courses = storage.loadCourses().stream()
                .filter(c -> departmentId.equals(c.getDepartmentId()))
                .collect(Collectors.toList());

        System.out.println("Courses in department: " + courses.size());

        if (courses.isEmpty()) {
            System.out.println("❌ No courses found for department: " + departmentId);
            return Collections.emptyList();
        }

        List<Teacher> deptTeachers = storage.loadTeachers().stream()
                .filter(t -> departmentId.equals(t.getDepartmentId()))
                .collect(Collectors.toList());
        
        System.out.println("Teachers in department: " + deptTeachers.size());

        List<String> theoryRooms = (theoryRoomPool == null || theoryRoomPool.isEmpty()) ? new ArrayList<>()
                : theoryRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());
        List<String> labRooms = (labRoomPool == null || labRoomPool.isEmpty()) ? new ArrayList<>()
                : labRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());

        System.out.println("Theory rooms: " + theoryRooms.size());
        System.out.println("Lab rooms: " + labRooms.size());

        // Create time slots if none provided
        if (timeSlots == null || timeSlots.isEmpty()) {
            timeSlots = Arrays.asList(
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                LocalTime.of(13, 30),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30)
            );
        }

        List<RoutineEntry> routine = new ArrayList<>();
        LocalDate currentDate = startDate;
        int dayIndex = 0;
        int slotIndex = 0;

        // Track used combinations
        Set<String> usedTeacherSlots = new HashSet<>();
        Set<String> usedRoomSlots = new HashSet<>();
        String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};

        for (Course course : courses) {
            System.out.println("\nAssigning course: " + course.getId());
            
            boolean assigned = false;
            int attempts = 0;
            int maxAttempts = 100;

            while (!assigned && attempts < maxAttempts) {
                attempts++;
                
                String day = days[dayIndex % days.length];
                LocalTime time = timeSlots.get(slotIndex % timeSlots.size());
                String timeSlot = time.toString();
                
                String teacherSlotKey = day + "_" + timeSlot;
                String roomSlotKey = day + "_" + timeSlot;

                List<Teacher> eligibleTeachers = deptTeachers.stream()
                        .filter(t -> t.getInterestedCourseCodes() != null && 
                                     t.getInterestedCourseCodes().contains(course.getId()))
                        .filter(t -> t.getProgramPreference() == course.getProgramType() || 
                                     t.getProgramPreference() == ProgramType.BOTH)
                        .filter(t -> t.getAvailableDays() != null && t.getAvailableDays().contains(day))
                        .filter(t -> t.getAvailableTimeSlots() != null && 
                                     t.getAvailableTimeSlots().contains(timeSlot))
                        .filter(t -> !usedTeacherSlots.contains(t.getTeacherId() + "_" + teacherSlotKey))
                        .collect(Collectors.toList());

                if (eligibleTeachers.isEmpty()) {
                    slotIndex++;
                    if (slotIndex >= timeSlots.size()) {
                        slotIndex = 0;
                        dayIndex++;
                    }
                    continue;
                }

                String roomId = null;
                List<String> availableRooms = course.isLab() ? labRooms : theoryRooms;
                
                for (String room : availableRooms) {
                    if (!usedRoomSlots.contains(room + "_" + roomSlotKey)) {
                        roomId = room;
                        break;
                    }
                }

                if (roomId == null) {
                    slotIndex++;
                    if (slotIndex >= timeSlots.size()) {
                        slotIndex = 0;
                        dayIndex++;
                    }
                    continue;
                }

                Teacher selectedTeacher = eligibleTeachers.get(0);
                
                RoutineEntry entry = new RoutineEntry();
                entry.setId(UUID.randomUUID().toString());
                entry.setDepartmentId(departmentId);
                entry.setCourseId(course.getId());
                entry.setCourseCode(course.getId());
                entry.setCourseTitle(course.getTitle());
                entry.setLabSession(course.isLab());
                entry.setTeacherId(selectedTeacher.getTeacherId());
                entry.setTeacherName(selectedTeacher.getName());
                entry.setRoomId(roomId);
                entry.setDate(currentDate.plusDays(dayIndex));
                entry.setTime(time);
                
                usedTeacherSlots.add(selectedTeacher.getTeacherId() + "_" + teacherSlotKey);
                usedRoomSlots.add(roomId + "_" + roomSlotKey);
                
                routine.add(entry);
                assigned = true;
                
                System.out.println("  ✅ Assigned: " + course.getId() + 
                                 " -> Teacher: " + selectedTeacher.getName() + 
                                 ", Room: " + roomId + 
                                 ", Day: " + day + 
                                 ", Time: " + timeSlot);
                
                slotIndex++;
                if (slotIndex >= timeSlots.size()) {
                    slotIndex = 0;
                    dayIndex++;
                }
            }

            if (!assigned) {
                System.out.println("❌ Failed to assign course: " + course.getId());
                return Collections.emptyList();
            }
        }

        storage.saveRoutine(routine);
        System.out.println("\n✅ Successfully generated " + routine.size() + " routine entries for department " + departmentId);
        return routine;
    }

    // ---------------- UI Helper ----------------
    public List<RoutineEntry> generate(Set<String> departmentIds, Set<String> batchIds) {
        System.out.println("\n=== GENERATE METHOD CALLED ===");
        System.out.println("Department IDs: " + departmentIds);
        System.out.println("Batch IDs: " + batchIds);
        
        List<Room> allRooms = storage.loadRooms();
        List<Room> theoryRooms = allRooms.stream()
                .filter(r -> r.getRoomType() == RoomType.THEORY)
                .collect(Collectors.toList());
        List<Room> labRooms = allRooms.stream()
                .filter(r -> r.getRoomType() == RoomType.LAB)
                .collect(Collectors.toList());

        LocalDate startDate = LocalDate.now().plusDays(1);
        List<LocalTime> timeSlots = Arrays.asList(
            LocalTime.of(8, 30),
            LocalTime.of(10, 0),
            LocalTime.of(11, 30),
            LocalTime.of(13, 30),
            LocalTime.of(15, 0),
            LocalTime.of(16, 30)
        );

        List<RoutineEntry> allRoutine = new ArrayList<>();

        if (departmentIds != null && !departmentIds.isEmpty()) {
            for (String deptId : departmentIds) {
                System.out.println("\nGenerating for department: " + deptId);
                List<RoutineEntry> deptRoutine = generateClassRoutineForDepartment(
                    deptId, startDate, timeSlots, null, theoryRooms, labRooms);
                allRoutine.addAll(deptRoutine);
            }
        }

        if (batchIds != null && !batchIds.isEmpty()) {
            for (String batchId : batchIds) {
                System.out.println("\nGenerating for batch: " + batchId);
                List<RoutineEntry> batchRoutine = generateClassRoutineForBatch(
                    batchId, startDate, timeSlots, null, theoryRooms, labRooms);
                allRoutine.addAll(batchRoutine);
            }
        }

        System.out.println("\n=== GENERATION COMPLETE ===");
        System.out.println("Total routine entries: " + allRoutine.size());
        
        return allRoutine;
    }

    // ---------------- Exam routines ----------------
    public List<Exam> generateExamRoutineForBatch(String batchId, LocalDate startDate, LocalDate endDate, String roomId) {
        Objects.requireNonNull(batchId);
        if (startDate == null || endDate == null) throw new IllegalArgumentException("startDate and endDate required");

        List<Course> courses = storage.loadCourses().stream()
                .filter(c -> batchId.equals(c.getBatchId()))
                .collect(Collectors.toList());

        if (courses.isEmpty()) return Collections.emptyList();

        List<LocalDate> altDates = new ArrayList<>();
        LocalDate d = startDate;
        boolean take = true;
        while (!d.isAfter(endDate)) {
            if (take) altDates.add(d);
            take = !take;
            d = d.plusDays(1);
        }

        if (altDates.size() < courses.size()) throw new IllegalArgumentException("Not enough alternate days");

        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            Exam ex = new Exam(courses.get(i).getId(), altDates.get(i));
            ex.setRoomNo(roomId);
            exams.add(ex);
        }

        storage.saveExamRoutine(batchId, exams);
        return exams;
    }

    public Map<String, List<Exam>> generateExamRoutineForDepartment(String departmentId, LocalDate startDate, LocalDate endDate, String roomId) {
        if (startDate == null || endDate == null) throw new IllegalArgumentException("startDate and endDate required");

        List<Course> courses = storage.loadCourses().stream()
                .filter(c -> departmentId.equals(c.getDepartmentId()))
                .collect(Collectors.toList());

        if (courses.isEmpty()) return Collections.emptyMap();

        List<LocalDate> altDates = new ArrayList<>();
        LocalDate d = startDate;
        boolean take = true;
        while (!d.isAfter(endDate)) {
            if (take) altDates.add(d);
            take = !take;
            d = d.plusDays(1);
        }

        if (altDates.size() < courses.size()) throw new IllegalArgumentException("Not enough alternate days");

        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            Exam ex = new Exam(courses.get(i).getId(), altDates.get(i));
            ex.setRoomNo(roomId);
            exams.add(ex);
        }

        String key = "DEPT::" + departmentId;
        storage.saveExamRoutine(key, exams);

        Map<String, List<Exam>> map = new HashMap<>();
        map.put(key, exams);
        return map;
    }
}