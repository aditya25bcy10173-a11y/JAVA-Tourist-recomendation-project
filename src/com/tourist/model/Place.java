package com.tourist.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a tourist destination with comprehensive attributes.
 */
public class Place {
    private String id;
    private String name;
    private String country;
    private String stateOrRegion;
    private Category primaryCategory;
    private List<Category> secondaryCategories;
    private BudgetTier budgetTier;
    private double estimatedDailyCostUSD; // Estimated base cost per person per day in USD
    private List<Season> bestSeasons;
    private List<CompanionType> suitableCompanions;
    private int minRecommendedDays;
    private int maxRecommendedDays;
    private double rating; // 1.0 to 5.0
    private String shortDescription;
    private String detailedDescription;
    private List<String> topAttractions;
    private List<String> activities;
    private List<String> localDelicacies;
    private String travelTips;
    private String sampleItinerary;

    public Place() {
        this.secondaryCategories = new ArrayList<>();
        this.bestSeasons = new ArrayList<>();
        this.suitableCompanions = new ArrayList<>();
        this.topAttractions = new ArrayList<>();
        this.activities = new ArrayList<>();
        this.localDelicacies = new ArrayList<>();
    }

    public Place(String id, String name, String country, String stateOrRegion,
                 Category primaryCategory, BudgetTier budgetTier, double estimatedDailyCostUSD,
                 double rating, int minDays, int maxDays,
                 String shortDescription) {
        this();
        this.id = id;
        this.name = name;
        this.country = country;
        this.stateOrRegion = stateOrRegion;
        this.primaryCategory = primaryCategory;
        this.budgetTier = budgetTier;
        this.estimatedDailyCostUSD = estimatedDailyCostUSD;
        this.rating = rating;
        this.minRecommendedDays = minDays;
        this.maxRecommendedDays = maxDays;
        this.shortDescription = shortDescription;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStateOrRegion() { return stateOrRegion; }
    public void setStateOrRegion(String stateOrRegion) { this.stateOrRegion = stateOrRegion; }

    public String getFullLocation() {
        if (stateOrRegion == null || stateOrRegion.trim().isEmpty()) {
            return country;
        }
        return stateOrRegion + ", " + country;
    }

    public Category getPrimaryCategory() { return primaryCategory; }
    public void setPrimaryCategory(Category primaryCategory) { this.primaryCategory = primaryCategory; }

    public List<Category> getSecondaryCategories() { return Collections.unmodifiableList(secondaryCategories); }
    public void addSecondaryCategory(Category category) {
        if (category != null && !this.secondaryCategories.contains(category)) {
            this.secondaryCategories.add(category);
        }
    }
    public void setSecondaryCategories(List<Category> categories) {
        this.secondaryCategories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
    }

    public boolean matchesCategory(Category category) {
        if (category == null) return true;
        if (this.primaryCategory == category) return true;
        return this.secondaryCategories.contains(category);
    }

    public BudgetTier getBudgetTier() { return budgetTier; }
    public void setBudgetTier(BudgetTier budgetTier) { this.budgetTier = budgetTier; }

    public double getEstimatedDailyCostUSD() { return estimatedDailyCostUSD; }
    public void setEstimatedDailyCostUSD(double estimatedDailyCostUSD) { this.estimatedDailyCostUSD = estimatedDailyCostUSD; }

    public List<Season> getBestSeasons() { return Collections.unmodifiableList(bestSeasons); }
    public void addBestSeason(Season season) {
        if (season != null && !this.bestSeasons.contains(season)) {
            this.bestSeasons.add(season);
        }
    }
    public void setBestSeasons(List<Season> seasons) {
        this.bestSeasons = seasons != null ? new ArrayList<>(seasons) : new ArrayList<>();
    }

    public boolean matchesSeason(Season season) {
        if (season == null || season == Season.ALL_YEAR) return true;
        if (this.bestSeasons.contains(Season.ALL_YEAR)) return true;
        return this.bestSeasons.contains(season);
    }

    public List<CompanionType> getSuitableCompanions() { return Collections.unmodifiableList(suitableCompanions); }
    public void addSuitableCompanion(CompanionType companion) {
        if (companion != null && !this.suitableCompanions.contains(companion)) {
            this.suitableCompanions.add(companion);
        }
    }
    public void setSuitableCompanions(List<CompanionType> companions) {
        this.suitableCompanions = companions != null ? new ArrayList<>(companions) : new ArrayList<>();
    }

    public boolean matchesCompanion(CompanionType companion) {
        if (companion == null) return true;
        return this.suitableCompanions.contains(companion);
    }

    public int getMinRecommendedDays() { return minRecommendedDays; }
    public void setMinRecommendedDays(int minRecommendedDays) { this.minRecommendedDays = minRecommendedDays; }

    public int getMaxRecommendedDays() { return maxRecommendedDays; }
    public void setMaxRecommendedDays(int maxRecommendedDays) { this.maxRecommendedDays = maxRecommendedDays; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }

    public List<String> getTopAttractions() { return Collections.unmodifiableList(topAttractions); }
    public void addTopAttraction(String attraction) {
        if (attraction != null && !attraction.trim().isEmpty()) {
            this.topAttractions.add(attraction.trim());
        }
    }
    public void setTopAttractions(List<String> attractions) {
        this.topAttractions = attractions != null ? new ArrayList<>(attractions) : new ArrayList<>();
    }

    public List<String> getActivities() { return Collections.unmodifiableList(activities); }
    public void addActivity(String activity) {
        if (activity != null && !activity.trim().isEmpty()) {
            this.activities.add(activity.trim());
        }
    }
    public void setActivities(List<String> activities) {
        this.activities = activities != null ? new ArrayList<>(activities) : new ArrayList<>();
    }

    public List<String> getLocalDelicacies() { return Collections.unmodifiableList(localDelicacies); }
    public void addLocalDelicacy(String delicacy) {
        if (delicacy != null && !delicacy.trim().isEmpty()) {
            this.localDelicacies.add(delicacy.trim());
        }
    }
    public void setLocalDelicacies(List<String> localDelicacies) {
        this.localDelicacies = localDelicacies != null ? new ArrayList<>(localDelicacies) : new ArrayList<>();
    }

    public String getTravelTips() { return travelTips; }
    public void setTravelTips(String travelTips) { this.travelTips = travelTips; }

    public String getSampleItinerary() { return sampleItinerary; }
    public void setSampleItinerary(String sampleItinerary) { this.sampleItinerary = sampleItinerary; }

    @Override
    public String toString() {
        return name + " (" + getFullLocation() + ") - " + primaryCategory.getDisplayName() + " [" + rating + "★]";
    }
}
