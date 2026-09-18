package com.tourist.test;

import com.tourist.engine.SuggestionEngine;
import com.tourist.engine.TripCostEstimator;
import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.CompanionType;
import com.tourist.model.Place;
import com.tourist.model.RecommendationResult;
import com.tourist.model.Season;
import com.tourist.model.TripCostEstimate;
import com.tourist.model.UserPreference;
import com.tourist.repository.PlaceRepository;
import com.tourist.repository.WishlistRepository;

import java.io.File;
import java.util.List;

/**
 * Automated test harness verifying recommendation logic, cost estimation,
 * search/filtering, and data persistence without third-party test dependencies.
 */
public class SuggestionEngineTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING TOURIST PLACES SYSTEM TEST SUITE");
        System.out.println("=================================================\n");

        testSeededDestinationsCount();
        testBeachBudgetRecommendation();
        testMountainWinterCoupleRecommendation();
        testStrictBudgetExclusion();
        testTripCostEstimatorCalculations();
        testSearchByQuery();
        testWishlistPersistence();

        System.out.println("\n=================================================");
        System.out.printf("  TEST RESULTS: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
            testsFailed++;
        }
    }

    private static void assertEquals(String testName, double expected, double actual, double delta) {
        if (Math.abs(expected - actual) <= delta) {
            System.out.println("  [PASS] " + testName + " (expected: " + expected + ", actual: " + actual + ")");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " (expected: " + expected + ", actual: " + actual + ")");
            testsFailed++;
        }
    }

    private static void testSeededDestinationsCount() {
        PlaceRepository repo = new PlaceRepository();
        List<Place> all = repo.getAllPlaces();
        assertTrue("Repository seeds at least 25 destinations", all.size() >= 25);
    }

    private static void testBeachBudgetRecommendation() {
        PlaceRepository repo = new PlaceRepository();
        SuggestionEngine engine = new SuggestionEngine();

        UserPreference pref = new UserPreference();
        pref.setPreferredCategory(Category.BEACH);
        pref.setBudgetTier(BudgetTier.BUDGET);
        pref.setTravelSeason(Season.WINTER);
        pref.setCompanionType(CompanionType.FRIENDS);
        pref.setDurationDays(4);

        List<RecommendationResult> results = engine.recommend(pref, repo.getAllPlaces());
        assertTrue("Results should not be empty for Beach & Budget", !results.isEmpty());

        RecommendationResult top = results.get(0);
        assertTrue("Top result should have high score (> 80%)", top.getMatchPercentage() >= 80.0);
        assertTrue("Top beach recommendation should match Category or Budget",
                top.getPlace().matchesCategory(Category.BEACH) || top.getPlace().getBudgetTier() == BudgetTier.BUDGET);
        assertTrue("Result reasons should explain the match", !top.getMatchReasons().isEmpty());
    }

    private static void testMountainWinterCoupleRecommendation() {
        PlaceRepository repo = new PlaceRepository();
        SuggestionEngine engine = new SuggestionEngine();

        UserPreference pref = new UserPreference();
        pref.setPreferredCategory(Category.MOUNTAIN);
        pref.setTravelSeason(Season.WINTER);
        pref.setCompanionType(CompanionType.COUPLE);
        pref.setDurationDays(5);

        List<RecommendationResult> results = engine.recommend(pref, repo.getAllPlaces());
        assertTrue("Should return recommendations for Mountain/Winter/Couple", !results.isEmpty());

        boolean hasManaliOrSwiss = false;
        for (int i = 0; i < Math.min(3, results.size()); i++) {
            String name = results.get(i).getPlace().getName();
            if (name.contains("Manali") || name.contains("Swiss") || name.contains("Banff")) {
                hasManaliOrSwiss = true;
                break;
            }
        }
        assertTrue("Top 3 suggestions contain alpine winter destination (Manali, Swiss Alps, or Banff)", hasManaliOrSwiss);
    }

    private static void testStrictBudgetExclusion() {
        PlaceRepository repo = new PlaceRepository();
        SuggestionEngine engine = new SuggestionEngine();

        UserPreference pref = new UserPreference();
        pref.setMaxDailyBudgetUSD(50.0);
        pref.setStrictBudget(true);

        List<RecommendationResult> results = engine.recommend(pref, repo.getAllPlaces());
        assertTrue("Strict budget filter returns affordable spots", !results.isEmpty());

        boolean allUnderBudget = true;
        for (RecommendationResult r : results) {
            if (r.getPlace().getEstimatedDailyCostUSD() > 50.0) {
                allUnderBudget = false;
                break;
            }
        }
        assertTrue("All recommendations strictly cost <= $50/day", allUnderBudget);
    }

    private static void testTripCostEstimatorCalculations() {
        PlaceRepository repo = new PlaceRepository();
        TripCostEstimator estimator = new TripCostEstimator();
        Place goa = repo.getById("P001"); // Goa, est. ~$42/day, Budget tier

        TripCostEstimate est = estimator.estimate(goa, 4, 2, BudgetTier.BUDGET);

        assertTrue("Total estimated cost should be greater than 0", est.getTotalEstimatedUSD() > 0);
        assertTrue("Lodging cost is positive", est.getLodgingCost() > 0);
        assertTrue("Food cost is positive", est.getFoodCost() > 0);
        assertTrue("Transit cost is positive", est.getLocalTransitCost() > 0);
        assertTrue("Contingency cost is positive", est.getMiscellaneousCost() > 0);

        double manualSubtotal = est.getLodgingCost() + est.getFoodCost() + est.getActivitiesCost() + est.getLocalTransitCost();
        assertEquals("Contingency is ~8% of subtotal", manualSubtotal * 0.08, est.getMiscellaneousCost(), 1.0);
    }

    private static void testSearchByQuery() {
        PlaceRepository repo = new PlaceRepository();
        List<Place> res = repo.search("Japan");
        assertTrue("Search 'Japan' returns at least Kyoto and Tokyo", res.size() >= 2);

        List<Place> safari = repo.search("safari");
        assertTrue("Search 'safari' returns safari destinations", !safari.isEmpty());
    }

    private static void testWishlistPersistence() {
        PlaceRepository placeRepo = new PlaceRepository();
        File tempFile = new File("build_test_wishlist.tmp");
        if (tempFile.exists()) tempFile.delete();

        WishlistRepository wishlist = new WishlistRepository(tempFile, placeRepo);
        Place p1 = placeRepo.getById("P001");
        Place p2 = placeRepo.getById("P004");

        wishlist.add(p1, "Goa beach party", "December");
        wishlist.add(p2, "Bali surfing trip", "July");

        assertTrue("Wishlist contains 2 items", wishlist.size() == 2);
        assertTrue("Wishlist contains P001", wishlist.contains("P001"));
        assertTrue("Wishlist contains P004", wishlist.contains("P004"));

        // Reload from file to verify persistence
        WishlistRepository reloaded = new WishlistRepository(tempFile, placeRepo);
        assertTrue("Reloaded wishlist preserves 2 items", reloaded.size() == 2);
        assertTrue("Reloaded wishlist preserves P001", reloaded.contains("P001"));

        // Clean up
        tempFile.delete();
    }
}
