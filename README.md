# 🌍 Tourist Places Suggestion System (Pure Java)

An intelligent, object-oriented recommendation engine developed entirely in **pure Java** (Standard Edition, zero external third-party dependencies required).

It assists travelers in finding optimal holiday spots, hill stations, beaches, cultural hubs, and adventures tailored to their exact **budget**, **travel season**, **companion group**, and **trip duration**.

---

## 🚀 Key Features

1. **Intelligent Recommendation Wizard**
   - **Multi-Factor Weighted Scoring Algorithm**:
     - *Category / Travel Theme Match (30%)*
     - *Budget Compatibility & Daily Limit (25%)*
     - *Seasonal / Weather Appropriateness (20%)*
     - *Group Dynamic Compatibility (15%)*
     - *Ideal Stay Duration Fit (10%)*
   - Explains **why** each destination is recommended with match reasons and tips (e.g. *"Matches your moderate budget"*, *"Peak travel season: Winter"*).

2. **Rich Built-In Destination Dataset (25+ Curated Places)**
   - Pre-loaded with iconic domestic and international spots:
     - **Beaches & Coastal**: Goa, Bali, Maldives, Andaman Islands, Amalfi Coast
     - **Mountains & Hill Stations**: Manali, Swiss Alps, Leh-Ladakh, Banff (Canada), Ooty
     - **Heritage & Culture**: Kyoto, Jaipur, Rome, Varanasi
     - **Adventure & Outdoors**: Rishikesh, Queenstown (NZ), Costa Rica, Grand Canyon
     - **Wildlife & Safari**: Serengeti (Tanzania), Jim Corbett
     - **Metropolitan & Modern**: Tokyo, Paris, Singapore, Dubai

3. **Dual User Interface (Desktop GUI + Terminal CLI)**
   - **Modern Swing Desktop GUI**:
     - Interactive recommendation questionnaire with instant results
     - Destination Explorer with keyword search, sorting, and category filters
     - Deep-dive Destination Dossier dialog (attractions, activities, delicacies, sample itinerary)
     - Interactive Trip Cost Estimator calculator
     - Saved Wishlist manager with file export
     - Dynamic "Add New Destination" modal
   - **Interactive Console CLI**:
     - Clean text-based menus, ASCII tables, and keyboard navigation for terminal environments.

4. **Detailed Trip Cost Estimator**
   - Calculates itemized budgets tailored to the traveler:
     - Lodging & Accommodation (rooms shared by group size)
     - Dining & Food expenses
     - Sightseeing & Activity entry fees
     - Local Transit (metro, taxis, cabs)
     - Safety/Contingency buffer (8%)
     - Total cost + per-traveler and per-day breakdown

5. **Wishlist & Itinerary Export**
   - Bookmark favorite destinations, note target travel months, add personal memos, and export a formatted travel plan directly to a text file.

---

## 📁 Project Structure

```
tourist-places-suggestion-system/
├── src/
│   └── com/
│       └── tourist/
│           ├── Main.java                        # Dual GUI/CLI launcher
│           ├── model/
│           │   ├── Category.java                # Travel themes enum
│           │   ├── BudgetTier.java              # Budget levels (Budget, Moderate, Luxury)
│           │   ├── Season.java                  # Seasons (Winter, Spring, Summer, Autumn, etc.)
│           │   ├── CompanionType.java           # Solo, Couple, Family, Friends
│           │   ├── Place.java                   # Core Destination model
│           │   ├── UserPreference.java          # Search and filter criteria
│           │   ├── RecommendationResult.java    # Scored suggestion with % and rationale
│           │   └── TripCostEstimate.java        # Itemized expense breakdown
│           ├── engine/
│           │   ├── SuggestionEngine.java        # Multi-factor recommendation algorithm
│           │   └── TripCostEstimator.java       # Expense calculator
│           ├── repository/
│           │   ├── PlaceRepository.java         # Seeded database and search/filter engine
│           │   └── WishlistRepository.java      # Local storage for saved places
│           ├── util/
│           │   └── FileStorageHelper.java       # Zero-dependency CSV and file helper
│           ├── ui/
│           │   ├── cli/
│           │   │   └── ConsoleUI.java           # Interactive terminal interface
│           │   └── gui/
│           │       ├── MainFrame.java           # Modern tabbed Swing window
│           │       ├── RecommendationPanel.java # Preference questionnaire & cards
│           │       ├── ExplorePanel.java        # Search, filter & tabular browser
│           │       ├── PlaceDetailDialog.java   # Sights, delicacies & sample itinerary
│           │       ├── CostEstimatorPanel.java  # Interactive budget calculator
│           │       ├── WishlistPanel.java       # Saved wishlist & itinerary export
│           │       └── AddPlaceDialog.java      # Form to add custom destinations
│           └── test/
│               └── SuggestionEngineTest.java    # Unit test suite
├── data/
│   ├── default_places.csv                       # Auto-generated external CSV dataset
│   └── user_wishlist.txt                        # Persisted wishlist
├── build_and_run.bat                            # Build and launch GUI
├── run_cli.bat                                  # Launch interactive CLI mode
├── run_gui.bat                                  # Launch desktop GUI mode
├── run_tests.bat                                # Run automated test suite
└── README.md
```

---

## 🛠 Compilation and Running

### Prerequisites
- Java JDK 11 or higher (Tested on **OpenJDK 21**).

### Option 1: Using Batch Scripts (Windows)
- **Launch GUI (Default)**:
  Double-click `build_and_run.bat` or run:
  ```cmd
  build_and_run.bat
  ```
- **Launch Terminal / Console Mode**:
  ```cmd
  run_cli.bat
  ```
- **Run Unit Tests**:
  ```cmd
  run_tests.bat
  ```

### Option 2: Manual Terminal Commands
From the `tourist-places-suggestion-system` root directory:

```bash
# 1. Compile all source files into bin/
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Windows PowerShell:
$files = Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName; javac -d bin -encoding UTF-8 $files

# 2. Run Desktop GUI
java -cp bin com.tourist.Main

# 3. Run Console CLI Mode
java -cp bin com.tourist.Main --cli

# 4. Run Automated Tests
java -cp bin com.tourist.test.SuggestionEngineTest
```
