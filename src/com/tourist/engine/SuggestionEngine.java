package com.tourist.engine;

import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.CompanionType;
import com.tourist.model.Place;
import com.tourist.model.RecommendationResult;
import com.tourist.model.Season;
import com.tourist.model.UserPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intelligent recommendation engine that scores tourist places based on
 * multiple criteria: category preferences, budget limits, seasonality,
 * companion dynamics, and ideal trip duration.
 */
public class SuggestionEngine {

    // Weight coefficients (Total = 100 points)
    private static final double WEIGHT_CATEGORY = 30.0;
    private static final double WEIGHT_BUDGET = 25.0;
    private static final double WEIGHT_SEASON = 20.0;
    private static final double WEIGHT_COMPANION = 15.0;
    private static final double WEIGHT_DURATION = 10.0;

    /**
     * Evaluates all candidate places against the user's preferences,
     * computing a weighted match score and human-readable explanations for each.
     */
    public List<RecommendationResult> recommend(UserPreference prefs, List<Place> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecommendationResult> results = new ArrayList<>();

        for (Place place : candidates) {
            // Apply text query filter if specified
            if (prefs.getDestinationQuery() != null && !prefs.getDestinationQuery().trim().isEmpty()) {
                String q = prefs.getDestinationQuery().trim().toLowerCase();
                boolean matchesName = place.getName().toLowerCase().contains(q);
                boolean matchesCountry = place.getCountry().toLowerCase().contains(q);
                boolean matchesRegion = place.getStateOrRegion() != null && place.getStateOrRegion().toLowerCase().contains(q);
                boolean matchesAttraction = place.getTopAttractions().stream().anyMatch(a -> a.toLowerCase().contains(q));

                if (!matchesName && !matchesCountry && !matchesRegion && !matchesAttraction) {
                    continue; // Skip if text search filter doesn't match
                }
            }

            // If strict budget is enabled and daily cost exceeds max budget, skip
            if (prefs.isStrictBudget() && prefs.getMaxDailyBudgetUSD() != null) {
                if (place.getEstimatedDailyCostUSD() > prefs.getMaxDailyBudgetUSD()) {
                    continue;
                }
            }

            RecommendationResult result = scorePlace(prefs, place);
            // Only include places that have at least some relevance (e.g. > 20% match)
            if (result.getMatchPercentage() >= 20.0) {
                results.add(result);
            }
        }

        Collections.sort(results);
        return results;
    }

    /**
     * Computes the match score and attaches granular explanations and cautions.
     */
    public RecommendationResult scorePlace(UserPreference prefs, Place place) {
        double categoryScore = evaluateCategory(prefs, place);
        double budgetScore = evaluateBudget(prefs, place);
        double seasonScore = evaluateSeason(prefs, place);
        double companionScore = evaluateCompanion(prefs, place);
        double durationScore = evaluateDuration(prefs, place);

        double totalScore = categoryScore + budgetScore + seasonScore + companionScore + durationScore;

        // Add subtle rating bonus (e.g., 4.9 rating adds ~2-3 points)
        double ratingBonus = (place.getRating() / 5.0) * 3.0;
        totalScore = Math.min(100.0, Math.max(0.0, totalScore + ratingBonus));

        RecommendationResult result = new RecommendationResult(place, totalScore);

        // Populate descriptive rationale
        explainCategory(prefs, place, result);
        explainBudget(prefs, place, result);
        explainSeason(prefs, place, result);
        explainCompanion(prefs, place, result);
        explainDuration(prefs, place, result);

        return result;
    }

    private double evaluateCategory(UserPreference prefs, Place place) {
        if (prefs.getPreferredCategory() == null) {
            return WEIGHT_CATEGORY * 0.75; // Neutral baseline if no category selected
        }

        if (place.getPrimaryCategory() == prefs.getPreferredCategory()) {
            return WEIGHT_CATEGORY; // 100% of category weight
        }

        if (place.getSecondaryCategories().contains(prefs.getPreferredCategory())) {
            return WEIGHT_CATEGORY * 0.75;
        }

        // Check if any secondary preferred categories match
        if (prefs.getSecondaryCategories() != null) {
            for (Category sec : prefs.getSecondaryCategories()) {
                if (place.getPrimaryCategory() == sec) {
                    return WEIGHT_CATEGORY * 0.65;
                }
                if (place.getSecondaryCategories().contains(sec)) {
                    return WEIGHT_CATEGORY * 0.50;
                }
            }
        }

        return WEIGHT_CATEGORY * 0.20; // Minor fallback
    }

    private double evaluateBudget(UserPreference prefs, Place place) {
        // Priority 1: Exact max daily budget specified
        if (prefs.getMaxDailyBudgetUSD() != null && prefs.getMaxDailyBudgetUSD() > 0) {
            double budget = prefs.getMaxDailyBudgetUSD();
            double cost = place.getEstimatedDailyCostUSD();

            if (cost <= budget) {
                // Fully within budget
                return WEIGHT_BUDGET;
            } else if (cost <= budget * 1.20) {
                // Up to 20% over budget
                return WEIGHT_BUDGET * 0.60;
            } else if (cost <= budget * 1.40) {
                return WEIGHT_BUDGET * 0.30;
            } else {
                return 0.0;
            }
        }

        // Priority 2: Budget Tier specified
        if (prefs.getBudgetTier() != null && place.getBudgetTier() != null) {
            int userLevel = prefs.getBudgetTier().getLevel();
            int placeLevel = place.getBudgetTier().getLevel();

            if (userLevel == placeLevel) {
                return WEIGHT_BUDGET;
            } else if (userLevel > placeLevel) {
                // User has higher budget, place is cheaper -> great value!
                return WEIGHT_BUDGET * 0.85;
            } else {
                // User has lower budget, place is more expensive -> penalty
                int diff = placeLevel - userLevel;
                if (diff == 1) return WEIGHT_BUDGET * 0.40;
                return WEIGHT_BUDGET * 0.15;
            }
        }

        return WEIGHT_BUDGET * 0.70; // Neutral baseline
    }

    private double evaluateSeason(UserPreference prefs, Place place) {
        if (prefs.getTravelSeason() == null || prefs.getTravelSeason() == Season.ALL_YEAR) {
            return WEIGHT_SEASON * 0.85;
        }

        if (place.getBestSeasons().contains(Season.ALL_YEAR)) {
            return WEIGHT_SEASON * 0.95;
        }

        if (place.getBestSeasons().contains(prefs.getTravelSeason())) {
            return WEIGHT_SEASON;
        }

        return WEIGHT_SEASON * 0.30; // Off-peak season
    }

    private double evaluateCompanion(UserPreference prefs, Place place) {
        if (prefs.getCompanionType() == null) {
            return WEIGHT_COMPANION * 0.80;
        }

        if (place.getSuitableCompanions().contains(prefs.getCompanionType())) {
            return WEIGHT_COMPANION;
        }

        return WEIGHT_COMPANION * 0.40;
    }

    private double evaluateDuration(UserPreference prefs, Place place) {
        if (prefs.getDurationDays() == null || prefs.getDurationDays() <= 0) {
            return WEIGHT_DURATION * 0.80;
        }

        int days = prefs.getDurationDays();
        if (days >= place.getMinRecommendedDays() && days <= place.getMaxRecommendedDays()) {
            return WEIGHT_DURATION;
        }

        int minDiff = Math.abs(days - place.getMinRecommendedDays());
        int maxDiff = Math.abs(days - place.getMaxRecommendedDays());
        int diff = Math.min(minDiff, maxDiff);

        if (diff <= 2) {
            return WEIGHT_DURATION * 0.70;
        }
        return WEIGHT_DURATION * 0.35;
    }

    // Explanation Builders
    private void explainCategory(UserPreference prefs, Place place, RecommendationResult result) {
        if (prefs.getPreferredCategory() == null) return;

        if (place.getPrimaryCategory() == prefs.getPreferredCategory()) {
            result.addMatchReason("Primary Theme: " + place.getPrimaryCategory().getDisplayName());
        } else if (place.getSecondaryCategories().contains(prefs.getPreferredCategory())) {
            result.addMatchReason("Secondary Theme: Features " + prefs.getPreferredCategory().getDisplayName());
        }
    }

    private void explainBudget(UserPreference prefs, Place place, RecommendationResult result) {
        if (prefs.getMaxDailyBudgetUSD() != null) {
            double budget = prefs.getMaxDailyBudgetUSD();
            double cost = place.getEstimatedDailyCostUSD();
            if (cost <= budget) {
                result.addMatchReason(String.format("Under Budget: ~$%.0f/day (your limit: $%.0f/day)", cost, budget));
            } else {
                result.addCaution(String.format("Exceeds Daily Budget: ~$%.0f/day vs your $%.0f/day limit", cost, budget));
            }
        } else if (prefs.getBudgetTier() != null) {
            if (prefs.getBudgetTier() == place.getBudgetTier()) {
                result.addMatchReason("Budget Match: " + place.getBudgetTier().getLabel());
            } else if (prefs.getBudgetTier().getLevel() > place.getBudgetTier().getLevel()) {
                result.addMatchReason("Economical Choice: " + place.getBudgetTier().getLabel());
            } else {
                result.addCaution("Higher Cost Tier: " + place.getBudgetTier().getLabel());
            }
        }
    }

    private void explainSeason(UserPreference prefs, Place place, RecommendationResult result) {
        if (prefs.getTravelSeason() == null || prefs.getTravelSeason() == Season.ALL_YEAR) return;

        if (place.getBestSeasons().contains(prefs.getTravelSeason())) {
            result.addMatchReason("Optimal Season: Perfect for " + prefs.getTravelSeason().getDisplayName());
        } else if (place.getBestSeasons().contains(Season.ALL_YEAR)) {
            result.addMatchReason("Year-Round Destination: Good weather during " + prefs.getTravelSeason().getDisplayName());
        } else {
            result.addCaution("Off-Peak Season: Prime months are " + place.getBestSeasons());
        }
    }

    private void explainCompanion(UserPreference prefs, Place place, RecommendationResult result) {
        if (prefs.getCompanionType() == null) return;

        if (place.getSuitableCompanions().contains(prefs.getCompanionType())) {
            result.addMatchReason("Party Fit: Highly rated for " + prefs.getCompanionType().getDisplayName());
        }
    }

    private void explainDuration(UserPreference prefs, Place place, RecommendationResult result) {
        if (prefs.getDurationDays() == null || prefs.getDurationDays() <= 0) return;

        int days = prefs.getDurationDays();
        if (days >= place.getMinRecommendedDays() && days <= place.getMaxRecommendedDays()) {
            result.addMatchReason(String.format("Ideal Stay: Fits your %d-day itinerary (%d-%d days recommended)",
                    days, place.getMinRecommendedDays(), place.getMaxRecommendedDays()));
        } else if (days < place.getMinRecommendedDays()) {
            result.addCaution(String.format("Short Stay: %d days is below recommended %d days",
                    days, place.getMinRecommendedDays()));
        } else {
            result.addMatchReason(String.format("Relaxed Pace: Can explore beyond key sights in %d days", days));
        }
    }
}
