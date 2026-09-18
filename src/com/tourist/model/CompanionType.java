package com.tourist.model;

/**
 * Represents group dynamics and travel party compositions.
 */
public enum CompanionType {
    SOLO("Solo Traveler", "Safe, walkable, backpacker friendly, rich with hostels and social tours"),
    COUPLE("Couples / Romantic", "Scenic vistas, romantic stays, intimate dinners, relaxing atmosphere"),
    FAMILY("Family with Kids & Elders", "Child-friendly, accessible, safe, amusement, rich cultural amenities"),
    FRIENDS("Group of Friends", "Vibrant nightlife, sports, adventure, group activities, shared villas");

    private final String displayName;
    private final String description;

    CompanionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static CompanionType fromString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String clean = text.trim().toUpperCase();
        for (CompanionType c : values()) {
            if (c.name().equalsIgnoreCase(clean) || c.displayName.toUpperCase().contains(clean)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
