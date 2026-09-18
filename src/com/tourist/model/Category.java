package com.tourist.model;

/**
 * Represents different travel themes and destination categories.
 */
public enum Category {
    BEACH("Beach & Coastal", "Sunny shores, watersports, and relaxing coastal getaways"),
    MOUNTAIN("Mountains & Hill Stations", "Scenic peaks, cool climate, trekking, and nature trails"),
    HERITAGE("Heritage & History", "Ancient architecture, monuments, ruins, and historical landmarks"),
    ADVENTURE("Adventure & Outdoor", "Extreme sports, hiking, rafting, and thrill-seeking activities"),
    WILDLIFE("Wildlife & Safari", "National parks, exotic fauna, birdwatching, and nature safaris"),
    CULTURAL("Culture & Spirituality", "Temples, festivals, sacred spots, local traditions, and spiritual retreats"),
    METROPOLITAN("Metropolitan & City Life", "Modern skylines, shopping, dining, museums, and vibrant nightlife");

    private final String displayName;
    private final String description;

    Category(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String clean = text.trim().toUpperCase().replace(" ", "_").replace("&", "").replace("-", "_");
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(clean) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        for (Category c : values()) {
            if (clean.contains(c.name()) || c.name().contains(clean)) {
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
