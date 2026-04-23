// ...existing code...
        // flatten rooms ids
-        List<String> theoryRooms = theoryRoomPool == null ? List.of() :
-                theoryRoomPool.stream().map(Room::getId).collect(Collectors.toList());
-        List<String> labRooms = labRoomPool == null ? List.of() :
-                labRoomPool.stream().map(Room::getId).collect(Collectors.toList());
+        List<String> theoryRooms = theoryRoomPool == null ? List.of() :
+                theoryRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());
+        List<String> labRooms = labRoomPool == null ? List.of() :
+                labRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());
 // ...existing code...
        for (Course c : courses) {
 // ...existing code...
        }
 
        // persist and return
        storage.saveRoutineEntries(out);
        return out;
    }
 
    // department-wide routine: collect all courses for department and assign similarly
    public List<RoutineEntry> generateClassRoutineForDepartment(String departmentId,
                                                                LocalDate startDate,
                                                                List<LocalTime> timeSlots,
                                                                Map<String,String> theoryRoomForCourse,
                                                                List<Room> theoryRoomPool,
                                                                List<Room> labRoomPool) {
        List<Course> courses = sService.getCoursesForDepartment(departmentId);
        if (courses.isEmpty()) return Collections.emptyList();
 
        // reuse single batch generator logic by creating temporary entries
        // (no batchId set, departmentId set)
        List<RoutineEntry> out = new ArrayList<>();
        LocalDate date = startDate;
        int slotIndex = 0;
        AtomicInteger theoryRoomPtr = new AtomicInteger(0);
        AtomicInteger labRoomPtr = new AtomicInteger(0);
-        List<String> theoryRooms = theoryRoomPool == null ? List.of() : theoryRoomPool.stream().map(Room::getId).collect(Collectors.toList());
-        List<String> labRooms = labRoomPool == null ? List.of() : labRoomPool.stream().map(Room::getId).collect(Collectors.toList());
+        List<String> theoryRooms = theoryRoomPool == null ? List.of() : theoryRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());
+        List<String> labRooms = labRoomPool == null ? List.of() : labRoomPool.stream().map(Room::getRoomNo).collect(Collectors.toList());
 
        for (Course c : courses) {
 // ...existing code...
        }
        storage.saveRoutineEntries(out);
        return out;
    }
 
+    // Simple UI-facing entry used by GenerateRoutineView
+    public List<RoutineEntry> generate(Set<String> departmentIds, Set<String> batchIds) {
+        List<RoutineEntry> out = new ArrayList<>();
+        // split available rooms into theory/lab pools
+        List<Room> allRooms = storage.loadRooms();
+        List<Room> theoryRooms = allRooms.stream().filter(r -> r.getRoomType() == RoomType.THEORY).collect(Collectors.toList());
+        List<Room> labRooms = allRooms.stream().filter(r -> r.getRoomType() == RoomType.LAB).collect(Collectors.toList());
+        LocalDate startDate = LocalDate.now().plusDays(1);
+
+        if (departmentIds != null) {
+            for (String d : departmentIds) {
+                out.addAll(generateClassRoutineForDepartment(d, startDate, null, null, theoryRooms, labRooms));
+            }
+        }
+        if (batchIds != null) {
+            for (String b : batchIds) {
+                out.addAll(generateClassRoutineForBatch(b, startDate, null, null, theoryRooms, labRooms));
+            }
+        }
+        return out;
+    }
+
 // ...existing code...