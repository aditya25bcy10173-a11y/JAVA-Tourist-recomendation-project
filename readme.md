🎯 Key Features Implemented
1. Multi-Criteria Recommendation Algorithm
Category Match (30%): Matches primary and secondary travel interests.
Budget Compatibility (25%): Compares daily accommodation, dining, and activity costs against the traveler's comfort tier or strict daily cap.
Seasonality (20%): Recommends destinations during their prime weather windows.
Group Dynamic (15%): Evaluates compatibility for Solo, Couples, Families with children/elders, or Groups of friends.
Duration Fit (10%): Checks if the planned number of days aligns with the destination's recommended length of stay.
Generates Match Percentage (0-100%) and transparent reasons (e.g., "Under Budget: ~$42/day (your limit: $50/day)", "Optimal Season: Perfect for Winter", "Party Fit: Highly rated for Group of Friends").
2. Pre-Seeded Dataset (25+ Curated Places)
Domestic and international destinations across all categories:
Beaches: Goa, Bali, Maldives, Andaman Islands, Amalfi Coast
Mountains: Manali, Swiss Alps, Banff (Canada), Leh-Ladakh, Ooty
Heritage: Kyoto, Jaipur, Rome, Varanasi
Adventure: Rishikesh, Queenstown (NZ), Costa Rica, Grand Canyon
Wildlife: Serengeti (Tanzania), Jim Corbett
Metropolitan: Tokyo, Paris, Singapore, Dubai
3. Dual Interface
Swing Desktop GUI:
RecommendationPanel: Interactive questionnaire with real-time match cards.
ExplorePanel: Full table browser with keyword search, sorting, and category filters.
PlaceDetailDialog: Complete dossier with overview, sights, activities, delicacies, tips, and multi-day itinerary.
CostEstimatorPanel: Interactive budget breakdown.
WishlistPanel: Saved destinations with file export.
AddPlaceDialog: Form to add custom spots on the fly.
Interactive Console CLI:
Numbered menus, ASCII tables, and interactive prompts.
4. Trip Cost Estimator
Dynamically estimates expenses:
Lodging: Automatically considers room sharing (1 room per 2 travelers).
Dining: Scaled per traveler per day.
Activities & Sightseeing: Entry tickets and adventures.
Local Transit: Cabs, shuttles, and metro passes.
Contingency Buffer: 8% safety cushion for unexpected expenses.
🧪 Verification & Test Results
1. Automated Unit Test Suite (SuggestionEngineTest)
All 22 unit tests passed with 0 failures:

Destination seeding verification (>= 25 destinations loaded).
Recommendation logic for Beach + Budget (Goa top-ranked with >80% match).
Recommendation logic for Mountain + Winter + Couple (Manali/Swiss Alps/Banff prioritized).
Strict budget exclusion (all returned places strictly 
≤
$
50
≤$50/day).
Mathematical accuracy of trip cost calculations & contingency buffer.
Search query indexing ("Japan", "safari").
Wishlist file persistence and reload round-trip.
text

=================================================
  RUNNING TOURIST PLACES SYSTEM TEST SUITE
=================================================
  [PASS] Repository seeds at least 25 destinations
  [PASS] Results should not be empty for Beach & Budget
  [PASS] Top result should have high score (> 80%)
  [PASS] Top beach recommendation should match Category or Budget
  [PASS] Result reasons should explain the match
  [PASS] Should return recommendations for Mountain/Winter/Couple
  [PASS] Top 3 suggestions contain alpine winter destination (Manali, Swiss Alps, or Banff)
  [PASS] Strict budget filter returns affordable spots
  [PASS] All recommendations strictly cost <= $50/day
  [PASS] Total estimated cost should be greater than 0
  [PASS] Lodging cost is positive
  [PASS] Food cost is positive
  [PASS] Transit cost is positive
  [PASS] Contingency cost is positive
  [PASS] Contingency is ~8% of subtotal (expected: 29.232, actual: 29.23)
  [PASS] Search 'Japan' returns at least Kyoto and Tokyo
  [PASS] Search 'safari' returns safari destinations
  [PASS] Wishlist contains 2 items
  [PASS] Wishlist contains P001
  [PASS] Wishlist contains P004
  [PASS] Reloaded wishlist preserves 2 items
  [PASS] Reloaded wishlist preserves P001
=================================================
  TEST RESULTS: 22 PASSED, 0 FAILED
=================================================
2. Interactive CLI Verification
Tested recommendation flow:
Input: Category = Beach & Coastal, Budget Tier = Budget Friendly, Max Daily = $50, Strict = Yes, Season = Winter, Companions = Friends, Duration = 4 days.
Output: Ranked Goa at #1 with 100% Match, followed by affordable domestic destinations (Rishikesh, Manali, Jaipur, Kerala, Varanasi).
Tested destination dossier inspection and search.
🚀 How to Run the Project
Windows Launchers:
Desktop Graphical Window: Double-click run_gui.bat or build_and_run.bat.
Terminal / Console Interface: Double-click run_cli.bat.
Run Unit Tests: Double-click run_tests.bat.
Command Line:
powershell

cd C:\Users\adity\.gemini\antigravity\scratch\tourist-places-suggestion-system
# Run GUI:
java -cp bin com.tourist.Main
# Run Interactive CLI:
java -cp bin com.tourist.Main --cli
# Run Tests:
java -cp bin com.tourist.test.SuggestionEngineTest
