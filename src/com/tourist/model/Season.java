package com.tourist.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents travel seasons and weather profiles.
 */
public enum Season {
    WINTER("Winter (Dec - Feb)", Arrays.asList("DECEMBER", "JANUARY", "FEBRUARY")),
    SPRING("Spring (Mar - May)", Arrays.asList("MARCH", "APRIL", "MAY")),
    SUMMER("Summer (Jun - Aug)", Arrays.asList("JUNE", "JULY", "AUGUST")),
    AUTUMN("Autumn (Sep - Nov)", Arrays.asList("SEPTEMBER", "OCTOBER", "NOVEMBER")),
    MONSOON("Monsoon / Rainy", Arrays.asList("JULY", "AUGUST", "SEPTEMBER")),
    ALL_YEAR("All Year Round", Arrays.asList(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    ));

    private final String displayName;
    private final List<String> months;

    Season(String displayName, List<String> months) {
        this.displayName = displayName;
        this.months = months;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getMonths() {
        return months;
    }

    public static Season fromString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String clean = text.trim().toUpperCase();
        for (Season s : values()) {
            if (s.name().equalsIgnoreCase(clean) || s.displayName.toUpperCase().contains(clean)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
