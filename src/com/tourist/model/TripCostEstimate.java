package com.tourist.model;

/**
 * Breakdown of estimated travel costs for a destination.
 */
public class TripCostEstimate {
    private final Place place;
    private final int numberOfDays;
    private final int numberOfTravelers;
    private final BudgetTier budgetTier;
    private final double lodgingCost;
    private final double foodCost;
    private final double activitiesCost;
    private final double localTransitCost;
    private final double miscellaneousCost;
    private final double totalEstimatedUSD;

    public TripCostEstimate(Place place, int numberOfDays, int numberOfTravelers, BudgetTier budgetTier,
                            double lodgingCost, double foodCost, double activitiesCost,
                            double localTransitCost, double miscellaneousCost) {
        this.place = place;
        this.numberOfDays = numberOfDays;
        this.numberOfTravelers = numberOfTravelers;
        this.budgetTier = budgetTier;
        this.lodgingCost = lodgingCost;
        this.foodCost = foodCost;
        this.activitiesCost = activitiesCost;
        this.localTransitCost = localTransitCost;
        this.miscellaneousCost = miscellaneousCost;
        this.totalEstimatedUSD = lodgingCost + foodCost + activitiesCost + localTransitCost + miscellaneousCost;
    }

    public Place getPlace() { return place; }
    public int getNumberOfDays() { return numberOfDays; }
    public int getNumberOfTravelers() { return numberOfTravelers; }
    public BudgetTier getBudgetTier() { return budgetTier; }
    public double getLodgingCost() { return lodgingCost; }
    public double getFoodCost() { return foodCost; }
    public double getActivitiesCost() { return activitiesCost; }
    public double getLocalTransitCost() { return localTransitCost; }
    public double getMiscellaneousCost() { return miscellaneousCost; }
    public double getTotalEstimatedUSD() { return totalEstimatedUSD; }

    public double getPerPersonCostUSD() {
        return numberOfTravelers > 0 ? totalEstimatedUSD / numberOfTravelers : totalEstimatedUSD;
    }

    public double getPerDayCostUSD() {
        return numberOfDays > 0 ? totalEstimatedUSD / numberOfDays : totalEstimatedUSD;
    }

    public String getFormattedSummary() {
        return String.format(
            "Trip Cost Estimate for %s (%d Days, %d Traveler%s)\n" +
            "Comfort Level: %s\n" +
            "---------------------------------------------------\n" +
            "Accommodation:       $%,.2f\n" +
            "Food & Dining:       $%,.2f\n" +
            "Activities & Entry:  $%,.2f\n" +
            "Local Transit:       $%,.2f\n" +
            "Contingency/Misc:    $%,.2f\n" +
            "---------------------------------------------------\n" +
            "TOTAL ESTIMATED:     $%,.2f (~ $%,.2f per traveler)",
            place.getName(), numberOfDays, numberOfTravelers, numberOfTravelers > 1 ? "s" : "",
            budgetTier.getLabel(), lodgingCost, foodCost, activitiesCost,
            localTransitCost, miscellaneousCost, totalEstimatedUSD, getPerPersonCostUSD()
        );
    }
}
