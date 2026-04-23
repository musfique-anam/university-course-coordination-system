package com.scheduler.model;

import java.io.Serializable;

/**
 * Fixed time slots:
 * 9:30-11:00, 11:10-12:40, 12:40-2:00 (Lunch), 2:00-3:10, 3:30-5:00
 */
public class TimeSlot implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SLOT_1 = "9:30-11:00";
    public static final String SLOT_2 = "11:10-12:40";
    public static final String LUNCH = "12:40-2:00";
    public static final String SLOT_3 = "2:00-3:10";
    public static final String SLOT_4 = "3:30-5:00";

    public static final String[] ALL_SLOTS = { SLOT_1, SLOT_2, LUNCH, SLOT_3, SLOT_4 };
    public static final String[] TEACHING_SLOTS = { SLOT_1, SLOT_2, SLOT_3, SLOT_4 };

    private String slotId;
    private String label;
    private boolean isLunch;

    public TimeSlot(String slotId, String label, boolean isLunch) {
        this.slotId = slotId;
        this.label = label;
        this.isLunch = isLunch;
    }

    public String getSlotId() { return slotId; }
    public String getLabel() { return label; }
    public boolean isLunch() { return isLunch; }
}
