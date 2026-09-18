package com.tourist.model;

/**
 * Categorizes destinations and travelers by budget comfort level.
 */
public enum BudgetTier {
    BUDGET("Budget Friendly", 1, 30, 70, "Hostels, street food, public transit, free attractions ($30-$70 / day)"),
    MODERATE("Moderate Comfort", 2, 70, 180, "3-4 star hotels, casual dining, guided tours, local taxis ($70-$180 / day)"),
    LUXURY("Luxury & Premium", 3, 180, 600, "5-star resorts, fine dining, private transfers, exclusive experiences ($180+ / day)");

    private final String label;
    private final int level; // 1 = low, 2 = mid, 3 = high
    private final int minDailyCost;
    private final int maxDailyCost;
    private final String description;

    BudgetTier(String label, int level, int minDailyCost, int maxDailyCost, String description) {
        this.label = label;
        this.level = level;
        this.minDailyCost = minDailyCost;
        this.maxDailyCost = maxDailyCost;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }

    public int getMinDailyCost() {
        return minDailyCost;
    }

    public int getMaxDailyCost() {
        return maxDailyCost;
    }

    public String getDescription() {
        return description;
    }

    public static BudgetTier fromString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String clean = text.trim().toUpperCase();
        for (BudgetTier b : values()) {
            if (b.name().equalsIgnoreCase(clean) || b.label.toUpperCase().contains(clean)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
