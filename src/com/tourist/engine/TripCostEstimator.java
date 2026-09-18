package com.tourist.engine;

import com.tourist.model.BudgetTier;
import com.tourist.model.Place;
import com.tourist.model.TripCostEstimate;

/**
 * Calculates itemized trip cost estimates (stay, dining, activities, local transit, contingency)
 * adapted to traveler count, duration, and target comfort level.
 */
public class TripCostEstimator {

    /**
     * Estimates cost using the place's native budget tier.
     */
    public TripCostEstimate estimate(Place place, int numberOfDays, int numberOfTravelers) {
        return estimate(place, numberOfDays, numberOfTravelers, place.getBudgetTier());
    }

    /**
     * Estimates cost tailored to an explicit budget tier comfort level.
     */
    public TripCostEstimate estimate(Place place, int numberOfDays, int numberOfTravelers, BudgetTier tier) {
        if (numberOfDays <= 0) numberOfDays = 1;
        if (numberOfTravelers <= 0) numberOfTravelers = 1;
        if (tier == null) tier = place.getBudgetTier();

        // Baseline daily cost per person in USD
        double baseDailyCost = place.getEstimatedDailyCostUSD();

        // Adjust baseline if traveler requested a tier different from the place's default
        double tierMultiplier = 1.0;
        if (place.getBudgetTier() != null) {
            int tierDiff = tier.getLevel() - place.getBudgetTier().getLevel();
            if (tierDiff == 1) tierMultiplier = 1.6;
            else if (tierDiff == 2) tierMultiplier = 2.8;
            else if (tierDiff == -1) tierMultiplier = 0.65;
            else if (tierDiff == -2) tierMultiplier = 0.40;
        }
        double adjustedDailyPerPerson = baseDailyCost * tierMultiplier;

        // 1. Accommodation (Hotel/Hostel/Resort)
        // Groups/couples share rooms (1 room per 2 travelers)
        int roomsNeeded = (int) Math.ceil(numberOfTravelers / 2.0);
        double roomNightRate = adjustedDailyPerPerson * 0.90; // A room typically costs ~0.9x of per-person daily budget * 2
        double lodgingCost = roomsNeeded * roomNightRate * numberOfDays;

        // 2. Food & Dining
        double dailyFoodPerPerson = adjustedDailyPerPerson * 0.32;
        double foodCost = dailyFoodPerPerson * numberOfTravelers * numberOfDays;

        // 3. Activities, Sightseeing & Entry Tickets
        double dailyActivityPerPerson = adjustedDailyPerPerson * 0.22;
        double activitiesCost = dailyActivityPerPerson * numberOfTravelers * numberOfDays;

        // 4. Local Transit (Metro, Taxis, Ferries, Tuk-tuks)
        // Group transport efficiency
        double dailyTransit = (adjustedDailyPerPerson * 0.15) * Math.max(1, numberOfTravelers * 0.65);
        double localTransitCost = dailyTransit * numberOfDays;

        // 5. Contingency buffer (8% of subtotal)
        double subtotal = lodgingCost + foodCost + activitiesCost + localTransitCost;
        double miscellaneousCost = subtotal * 0.08;

        return new TripCostEstimate(
            place,
            numberOfDays,
            numberOfTravelers,
            tier,
            round(lodgingCost),
            round(foodCost),
            round(activitiesCost),
            round(localTransitCost),
            round(miscellaneousCost)
        );
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
