package com.tourist.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the user's travel preferences for recommendation filtering and scoring.
 */
public class UserPreference {
    private Category preferredCategory;
    private List<Category> secondaryCategories;
    private BudgetTier budgetTier;
    private Double maxDailyBudgetUSD;
    private Season travelSeason;
    private CompanionType companionType;
    private Integer durationDays;
    private String destinationQuery; // country or city keyword
    private boolean strictBudget;

    public UserPreference() {
        this.secondaryCategories = new ArrayList<>();
    }

    public Category getPreferredCategory() { return preferredCategory; }
    public void setPreferredCategory(Category preferredCategory) { this.preferredCategory = preferredCategory; }

    public List<Category> getSecondaryCategories() { return secondaryCategories; }
    public void addSecondaryCategory(Category category) {
        if (category != null && !secondaryCategories.contains(category)) {
            secondaryCategories.add(category);
        }
    }
    public void setSecondaryCategories(List<Category> categories) {
        this.secondaryCategories = categories != null ? categories : new ArrayList<>();
    }

    public BudgetTier getBudgetTier() { return budgetTier; }
    public void setBudgetTier(BudgetTier budgetTier) { this.budgetTier = budgetTier; }

    public Double getMaxDailyBudgetUSD() { return maxDailyBudgetUSD; }
    public void setMaxDailyBudgetUSD(Double maxDailyBudgetUSD) { this.maxDailyBudgetUSD = maxDailyBudgetUSD; }

    public Season getTravelSeason() { return travelSeason; }
    public void setTravelSeason(Season travelSeason) { this.travelSeason = travelSeason; }

    public CompanionType getCompanionType() { return companionType; }
    public void setCompanionType(CompanionType companionType) { this.companionType = companionType; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getDestinationQuery() { return destinationQuery; }
    public void setDestinationQuery(String destinationQuery) { this.destinationQuery = destinationQuery; }

    public boolean isStrictBudget() { return strictBudget; }
    public void setStrictBudget(boolean strictBudget) { this.strictBudget = strictBudget; }
}
