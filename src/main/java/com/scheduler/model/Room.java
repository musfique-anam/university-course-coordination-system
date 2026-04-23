package com.scheduler.model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;           // This will map to "id" in JSON
    private String roomNo;        // Room number
    private int capacity;
    private RoomType roomType;
    private boolean hasMultimedia;
    private String departmentId;
    private int floor;
    private boolean hasProjector;
    private boolean hasSoundSystem;
    private boolean available;
    private int totalPCs;
    private int activePCs;

    public Room() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public boolean isHasMultimedia() { return hasMultimedia; }
    public void setHasMultimedia(boolean hasMultimedia) { this.hasMultimedia = hasMultimedia; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }

    public boolean isHasSoundSystem() { return hasSoundSystem; }
    public void setHasSoundSystem(boolean hasSoundSystem) { this.hasSoundSystem = hasSoundSystem; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getTotalPCs() { return totalPCs; }
    public void setTotalPCs(int totalPCs) { this.totalPCs = totalPCs; }

    public int getActivePCs() { return activePCs; }
    public void setActivePCs(int activePCs) { this.activePCs = activePCs; }
}