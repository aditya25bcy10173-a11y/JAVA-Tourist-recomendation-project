package com.tourist.ui.cli;

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

import java.util.List;
import java.util.Scanner;

/**
 * Interactive text console UI for terminal users.
 */
public class ConsoleUI {

    private final PlaceRepository placeRepo;
    private final WishlistRepository wishlistRepo;
    private final SuggestionEngine suggestionEngine;
    private final TripCostEstimator costEstimator;
    private final Scanner scanner;

    public ConsoleUI(PlaceRepository placeRepo, WishlistRepository wishlistRepo,
                     SuggestionEngine suggestionEngine, TripCostEstimator costEstimator) {
        this.placeRepo = placeRepo;
        this.wishlistRepo = wishlistRepo;
        this.suggestionEngine = suggestionEngine;
        this.costEstimator = costEstimator;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("\n➤ Enter choice [0-6]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleRecommendationWizard();
                    break;
                case "2":
                    handleExplorePlaces();
                    break;
                case "3":
                    handlePlaceDetails();
                    break;
                case "4":
                    handleTripCostCalculator();
                    break;
                case "5":
                    handleWishlistMenu();
                    break;
                case "6":
                    handleAddCustomPlace();
                    break;
                case "0":
                    running = false;
                    System.out.println("\n✈ Thank you for using Tourist Places Suggestion System. Safe travels!\n");
                    break;
                default:
                    System.out.println("❌ Invalid option. Please enter a number between 0 and 6.");
            }
        }
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║            🌍 TOURIST PLACES SUGGESTION SYSTEM 🌍                ║");
        System.out.println("║           Smart Travel Recommendations Powered by Java           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    private void printMainMenu() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━ MAIN MENU ━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println(" [1] 🎯 Smart Recommendation Wizard (Personalized Match)");
        System.out.println(" [2] 🔍 Explore & Search All Destinations");
        System.out.println(" [3] 📖 View Detailed Destination Dossier & Itinerary");
        System.out.println(" [4] 💰 Trip Cost & Budget Estimator");
        System.out.println(" [5] ⭐ My Travel Wishlist (" + wishlistRepo.size() + " saved)");
        System.out.println(" [6] ➕ Add a Custom Tourist Destination");
        System.out.println(" [0] 🚪 Exit");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void handleRecommendationWizard() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║            🎯 PERSONALIZED RECOMMENDATION WIZARD                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("Answer a few quick questions to find your dream destination!\n");

        UserPreference prefs = new UserPreference();

        // 1. Travel Theme
        System.out.println("1️⃣ Select your preferred travel theme / category:");
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("   [%d] %s (%s)\n", i + 1, categories[i].getDisplayName(), categories[i].getDescription());
        }
        System.out.print("   Select theme [1-" + categories.length + ", or Enter to skip]: ");
        String catInput = scanner.nextLine().trim();
        if (!catInput.isEmpty()) {
            try {
                int idx = Integer.parseInt(catInput) - 1;
                if (idx >= 0 && idx < categories.length) {
                    prefs.setPreferredCategory(categories[idx]);
                }
            } catch (NumberFormatException ignored) {}
        }

        // 2. Budget Level
        System.out.println("\n2️⃣ Select your preferred budget level:");
        BudgetTier[] tiers = BudgetTier.values();
        for (int i = 0; i < tiers.length; i++) {
            System.out.printf("   [%d] %s - %s\n", i + 1, tiers[i].getLabel(), tiers[i].getDescription());
        }
        System.out.print("   Select budget tier [1-3, or Enter to skip]: ");
        String tierInput = scanner.nextLine().trim();
        if (!tierInput.isEmpty()) {
            try {
                int idx = Integer.parseInt(tierInput) - 1;
                if (idx >= 0 && idx < tiers.length) {
                    prefs.setBudgetTier(tiers[idx]);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Optional maximum daily cost
        System.out.print("   Optional: Set strict max daily budget in USD per person (e.g. 50, or Enter to skip): $");
        String maxBudget = scanner.nextLine().trim();
        if (!maxBudget.isEmpty()) {
            try {
                double budget = Double.parseDouble(maxBudget);
                if (budget > 0) {
                    prefs.setMaxDailyBudgetUSD(budget);
                    System.out.print("   Strict budget cap? [y/N]: ");
                    prefs.setStrictBudget(scanner.nextLine().trim().equalsIgnoreCase("y"));
                }
            } catch (NumberFormatException ignored) {}
        }

        // 3. Travel Season
        System.out.println("\n3️⃣ What season do you plan to travel in?");
        Season[] seasons = Season.values();
        for (int i = 0; i < seasons.length; i++) {
            System.out.printf("   [%d] %s\n", i + 1, seasons[i].getDisplayName());
        }
        System.out.print("   Select season [1-" + seasons.length + ", or Enter to skip]: ");
        String seasonInput = scanner.nextLine().trim();
        if (!seasonInput.isEmpty()) {
            try {
                int idx = Integer.parseInt(seasonInput) - 1;
                if (idx >= 0 && idx < seasons.length) {
                    prefs.setTravelSeason(seasons[idx]);
                }
            } catch (NumberFormatException ignored) {}
        }

        // 4. Companion
        System.out.println("\n4️⃣ Who are you traveling with?");
        CompanionType[] companions = CompanionType.values();
        for (int i = 0; i < companions.length; i++) {
            System.out.printf("   [%d] %s - %s\n", i + 1, companions[i].getDisplayName(), companions[i].getDescription());
        }
        System.out.print("   Select companion [1-" + companions.length + ", or Enter to skip]: ");
        String compInput = scanner.nextLine().trim();
        if (!compInput.isEmpty()) {
            try {
                int idx = Integer.parseInt(compInput) - 1;
                if (idx >= 0 && idx < companions.length) {
                    prefs.setCompanionType(companions[idx]);
                }
            } catch (NumberFormatException ignored) {}
        }

        // 5. Trip Duration
        System.out.print("\n5️⃣ How many days is your trip? (e.g. 4, or Enter to skip): ");
        String durInput = scanner.nextLine().trim();
        if (!durInput.isEmpty()) {
            try {
                int days = Integer.parseInt(durInput);
                if (days > 0) prefs.setDurationDays(days);
            } catch (NumberFormatException ignored) {}
        }

        // Run Recommendation Engine
        System.out.println("\n⏳ Analyzing destinations and calculating match scores...");
        List<RecommendationResult> results = suggestionEngine.recommend(prefs, placeRepo.getAllPlaces());

        if (results.isEmpty()) {
            System.out.println("⚠️ No destinations found that match your strict criteria. Try widening your budget or season.");
            return;
        }

        System.out.println("\n✨ TOP RECOMMENDED DESTINATIONS FOR YOU ✨");
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════");
        int count = 1;
        for (RecommendationResult res : results) {
            Place p = res.getPlace();
            System.out.printf(" #%d  [%3d%% MATCH]  %-24s | %-16s | Rating: %.1f★ | ~$%.0f/day\n",
                    count++, res.getRoundedScore(), p.getName(), p.getFullLocation(), p.getRating(), p.getEstimatedDailyCostUSD());
            System.out.println("     Theme: " + p.getPrimaryCategory().getDisplayName() + " | Comfort: " + p.getBudgetTier().getLabel());
            System.out.println("     Highlights: " + String.join(", ", p.getTopAttractions()));

            if (!res.getMatchReasons().isEmpty()) {
                System.out.println("     ✅ Why it fits: " + String.join(" | ", res.getMatchReasons()));
            }
            if (!res.getCautions().isEmpty()) {
                System.out.println("     ⚠️ Note: " + String.join(" | ", res.getCautions()));
            }
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
            if (count > 6) { // Display top 6
                System.out.printf("  ... and %d more matching destinations.\n", results.size() - 6);
                break;
            }
        }

        promptQuickAction(results);
    }

    private void promptQuickAction(List<RecommendationResult> results) {
        System.out.println("\nOptions: Enter destination name or rank # (e.g. '1' or 'Manali') for full details, or Enter to return to menu.");
        System.out.print("➤ Your choice: ");
        String choice = scanner.nextLine().trim();
        if (choice.isEmpty()) return;

        Place selected = null;
        try {
            int rank = Integer.parseInt(choice);
            if (rank >= 1 && rank <= results.size()) {
                selected = results.get(rank - 1).getPlace();
            }
        } catch (NumberFormatException ignored) {}

        if (selected == null) {
            for (RecommendationResult res : results) {
                if (res.getPlace().getName().equalsIgnoreCase(choice)) {
                    selected = res.getPlace();
                    break;
                }
            }
        }

        if (selected != null) {
            displayFullPlaceDossier(selected);
        } else {
            System.out.println("Place not recognized.");
        }
    }

    private void handleExplorePlaces() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║            🔍 EXPLORE & SEARCH ALL DESTINATIONS                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("Filter by keyword, theme, or view all.\n");

        System.out.print("➤ Search keyword (place name, country, attraction, or Enter for all): ");
        String query = scanner.nextLine().trim();

        List<Place> matches = placeRepo.search(query);
        if (matches.isEmpty()) {
            System.out.println("❌ No destinations matched your search query: '" + query + "'");
            return;
        }

        System.out.printf("\nFound %d destinations:\n", matches.size());
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf(" %-6s | %-20s | %-18s | %-16s | %-10s | %s\n",
                "ID", "Name", "Location", "Category", "Est. Daily", "Rating");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        for (Place p : matches) {
            System.out.printf(" %-6s | %-20s | %-18s | %-16s | $%-9.0f | %.1f★\n",
                    p.getId(), truncate(p.getName(), 20), truncate(p.getFullLocation(), 18),
                    truncate(p.getPrimaryCategory().getDisplayName(), 16),
                    p.getEstimatedDailyCostUSD(), p.getRating());
        }
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════");

        System.out.print("\nEnter Place ID (e.g. P001) to view full dossier & itinerary, or Enter to go back: ");
        String id = scanner.nextLine().trim();
        if (!id.isEmpty()) {
            Place p = placeRepo.getById(id.toUpperCase());
            if (p != null) {
                displayFullPlaceDossier(p);
            } else {
                System.out.println("Destination with ID '" + id + "' not found.");
            }
        }
    }

    private void handlePlaceDetails() {
        System.out.print("\n➤ Enter Place Name or ID to view details (e.g. 'Goa' or 'P002'): ");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) return;

        Place p = placeRepo.getById(query.toUpperCase());
        if (p == null) {
            List<Place> found = placeRepo.search(query);
            if (!found.isEmpty()) {
                p = found.get(0);
            }
        }

        if (p != null) {
            displayFullPlaceDossier(p);
        } else {
            System.out.println("Destination not found.");
        }
    }

    private void displayFullPlaceDossier(Place p) {
        System.out.println("\n╔═════════════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║  📖 DESTINATION DOSSIER: %-48s ║\n", p.getName().toUpperCase() + " (" + p.getCountry() + ")");
        System.out.println("╚═════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("📍 Full Location:     " + p.getFullLocation());
        System.out.println("🏷  Primary Theme:     " + p.getPrimaryCategory().getDisplayName());
        if (!p.getSecondaryCategories().isEmpty()) {
            System.out.println("✨ Also Great For:    " + p.getSecondaryCategories());
        }
        System.out.println("⭐ User Rating:       " + p.getRating() + " / 5.0 ★");
        System.out.println("💵 Budget Comfort:    " + p.getBudgetTier().getLabel() + " (~$" + p.getEstimatedDailyCostUSD() + " USD / day)");
        System.out.println("⏳ Ideal Duration:    " + p.getMinRecommendedDays() + " to " + p.getMaxRecommendedDays() + " Days");
        System.out.println("☀️ Best Seasons:      " + p.getBestSeasons());
        System.out.println("👥 Best Suited For:   " + p.getSuitableCompanions());
        System.out.println("\n📝 Overview:");
        System.out.println("   " + p.getShortDescription());

        System.out.println("\n🏛 Top Sights & Attractions:");
        for (String sight : p.getTopAttractions()) {
            System.out.println("   • " + sight);
        }

        System.out.println("\n🏄 Popular Activities:");
        for (String act : p.getActivities()) {
            System.out.println("   • " + act);
        }

        System.out.println("\n🍲 Must-Try Local Food & Delicacies:");
        for (String food : p.getLocalDelicacies()) {
            System.out.println("   • " + food);
        }

        if (p.getTravelTips() != null && !p.getTravelTips().isEmpty()) {
            System.out.println("\n💡 Pro Travel Tip:");
            System.out.println("   " + p.getTravelTips());
        }

        if (p.getSampleItinerary() != null && !p.getSampleItinerary().isEmpty()) {
            System.out.println("\n📅 Suggested Itinerary Outline:");
            String[] lines = p.getSampleItinerary().split("\n");
            for (String l : lines) {
                System.out.println("   " + l);
            }
        }

        System.out.println("\n─────────────────────────────────────────────────────────────────────────────────");
        System.out.print("Options: [W] Save to Wishlist | [C] Calculate Trip Cost | [Enter] Back: ");
        String action = scanner.nextLine().trim().toUpperCase();

        if (action.equals("W")) {
            System.out.print("Enter personal trip notes (or leave blank): ");
            String notes = scanner.nextLine().trim();
            System.out.print("Target travel time (e.g. 'December', 'Next Summer', or Enter for Flexible): ");
            String season = scanner.nextLine().trim();
            wishlistRepo.add(p, notes, season.isEmpty() ? "Flexible" : season);
            System.out.println("⭐ " + p.getName() + " saved to your Wishlist!");
        } else if (action.equals("C")) {
            runCostEstimatorForPlace(p);
        }
    }

    private void handleTripCostCalculator() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║            💰 TRIP COST & BUDGET ESTIMATOR CALCULATOR            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        System.out.print("➤ Enter destination name or ID (e.g. 'Goa', 'Bali', 'Swiss Alps'): ");
        String placeQuery = scanner.nextLine().trim();
        Place place = placeRepo.getById(placeQuery.toUpperCase());
        if (place == null) {
            List<Place> matches = placeRepo.search(placeQuery);
            if (!matches.isEmpty()) {
                place = matches.get(0);
            }
        }

        if (place == null) {
            System.out.println("❌ Destination not found. Please check spelling.");
            return;
        }

        runCostEstimatorForPlace(place);
    }

    private void runCostEstimatorForPlace(Place place) {
        System.out.println("\nCalculating trip costs for: " + place.getName() + " (" + place.getFullLocation() + ")");

        System.out.print("➤ Number of days (recommended " + place.getMinRecommendedDays() + "-" + place.getMaxRecommendedDays() + " days): ");
        int days = 4;
        try {
            days = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ignored) {}

        System.out.print("➤ Number of travelers: ");
        int travelers = 2;
        try {
            travelers = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ignored) {}

        System.out.println("\nSelect comfort level:");
        System.out.println(" [1] Budget Friendly (Hostels, street food, public transport)");
        System.out.println(" [2] Moderate Comfort (3-4 star hotels, casual restaurants, tours)");
        System.out.println(" [3] Luxury & Premium (5-star resorts, fine dining, private transfers)");
        System.out.print("➤ Select comfort [1-3, or Enter for default]: ");
        String tierChoice = scanner.nextLine().trim();

        BudgetTier tier = place.getBudgetTier();
        if ("1".equals(tierChoice)) tier = BudgetTier.BUDGET;
        else if ("2".equals(tierChoice)) tier = BudgetTier.MODERATE;
        else if ("3".equals(tierChoice)) tier = BudgetTier.LUXURY;

        TripCostEstimate estimate = costEstimator.estimate(place, days, travelers, tier);

        System.out.println("\n" + estimate.getFormattedSummary());
    }

    private void handleWishlistMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ⭐ MY TRAVEL WISHLIST                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        List<WishlistRepository.WishlistItem> items = wishlistRepo.getAll();
        if (items.isEmpty()) {
            System.out.println("Your wishlist is currently empty! Use the Recommendation Wizard or Explore to add spots.");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            WishlistRepository.WishlistItem item = items.get(i);
            Place p = item.getPlace();
            System.out.printf(" [%d] %-20s | %-16s | Target: %-12s | ~$%.0f/day\n",
                    i + 1, p.getName(), p.getFullLocation(), item.getTargetSeasonOrMonth(), p.getEstimatedDailyCostUSD());
            if (!item.getNotes().isEmpty()) {
                System.out.println("     Notes: " + item.getNotes());
            }
        }

        System.out.println("\nOptions: [R] Remove item | [P] Print/Export Summary | [Enter] Back");
        System.out.print("➤ Choice: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("R")) {
            System.out.print("Enter number to remove: ");
            try {
                int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (idx >= 0 && idx < items.size()) {
                    String removedName = items.get(idx).getPlace().getName();
                    wishlistRepo.remove(items.get(idx).getPlace().getId());
                    System.out.println("🗑 Removed '" + removedName + "' from your wishlist.");
                }
            } catch (NumberFormatException ignored) {}
        } else if (choice.equals("P")) {
            System.out.println("\n" + wishlistRepo.exportSummary());
        }
    }

    private void handleAddCustomPlace() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║               ➕ ADD A NEW TOURIST DESTINATION                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        try {
            System.out.print("➤ Destination Name (e.g. Shimla): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) return;

            System.out.print("➤ Country (e.g. India): ");
            String country = scanner.nextLine().trim();

            System.out.print("➤ State or Region (e.g. Himachal Pradesh): ");
            String region = scanner.nextLine().trim();

            System.out.println("Select Category:");
            Category[] categories = Category.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.printf(" [%d] %s\n", i + 1, categories[i].getDisplayName());
            }
            System.out.print("Choice [1-" + categories.length + "]: ");
            int catIdx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            Category category = (catIdx >= 0 && catIdx < categories.length) ? categories[catIdx] : Category.BEACH;

            System.out.println("Select Budget Tier: [1] Budget  [2] Moderate  [3] Luxury");
            System.out.print("Choice: ");
            String tierStr = scanner.nextLine().trim();
            BudgetTier tier = "3".equals(tierStr) ? BudgetTier.LUXURY : ("2".equals(tierStr) ? BudgetTier.MODERATE : BudgetTier.BUDGET);

            System.out.print("➤ Estimated daily cost in USD per person (e.g. 45): $");
            double dailyCost = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("➤ User Rating (1.0 to 5.0, e.g. 4.6): ");
            double rating = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("➤ Recommended duration (minimum days, e.g. 3): ");
            int minDays = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("➤ Recommended duration (maximum days, e.g. 5): ");
            int maxDays = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("➤ Short description: ");
            String description = scanner.nextLine().trim();

            System.out.print("➤ Top attractions (comma separated, e.g. Mall Road, Jakhoo Temple): ");
            String sights = scanner.nextLine().trim();

            String newId = "CUSTOM_" + System.currentTimeMillis() % 10000;
            Place place = new Place(newId, name, country, region, category, tier, dailyCost, rating, minDays, maxDays, description);
            if (!sights.isEmpty()) {
                for (String s : sights.split(",")) {
                    place.addTopAttraction(s.trim());
                }
            }
            place.addBestSeason(Season.ALL_YEAR);
            place.addSuitableCompanion(CompanionType.FAMILY);
            place.addSuitableCompanion(CompanionType.COUPLE);
            place.addSuitableCompanion(CompanionType.FRIENDS);

            placeRepo.addPlace(place);
            System.out.println("\n✅ Successfully added '" + name + "' to the tourist destinations directory!");
        } catch (Exception e) {
            System.out.println("❌ Failed to add destination due to invalid input: " + e.getMessage());
        }
    }

    private String truncate(String text, int len) {
        if (text == null) return "";
        if (text.length() <= len) return text;
        return text.substring(0, len - 3) + "...";
    }
}
