package com.scheduler.util;

import java.util.Arrays;
import java.util.List;

/**
 * Time rules: HSC Sat-Tue, Diploma Fri-Sat. Fixed slots with lunch break.
 */
public final class TimeRules {

    // Working days
    public static final List<String> HSC_DAYS = Arrays.asList("Saturday", "Sunday", "Monday", "Tuesday");
    public static final List<String> DIPLOMA_DAYS = Arrays.asList("Friday", "Saturday");

    // Teaching slots (no lunch)
    public static final String[] TEACHING_SLOTS = {"9:30-11:00", "11:10-12:40", "2:00-3:10", "3:30-5:00"};
    public static final String LUNCH = "12:40-2:00";

    public static List<String> getWorkingDays(String programType) {
        if ("HSC".equalsIgnoreCase(programType)) return HSC_DAYS;
        if ("DIPLOMA".equalsIgnoreCase(programType)) return DIPLOMA_DAYS;
        return HSC_DAYS;
    }
}
