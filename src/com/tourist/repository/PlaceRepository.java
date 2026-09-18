package com.tourist.repository;

import com.tourist.model.BudgetTier;
import com.tourist.model.Category;
import com.tourist.model.CompanionType;
import com.tourist.model.Place;
import com.tourist.model.Season;
import com.tourist.util.FileStorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data access and in-memory cache for all tourist places.
 * Pre-seeded with 30+ diverse destinations and supports importing/exporting to CSV.
 */
public class PlaceRepository {

    private final Map<String, Place> placesMap = new LinkedHashMap<>();

    public PlaceRepository() {
        seedInitialDestinations();
    }

    public List<Place> getAllPlaces() {
        return new ArrayList<>(placesMap.values());
    }

    public Place getById(String id) {
        return placesMap.get(id);
    }

    public void addPlace(Place place) {
        if (place != null && place.getId() != null) {
            placesMap.put(place.getId(), place);
        }
    }

    public boolean removePlace(String id) {
        return placesMap.remove(id) != null;
    }

    public List<Place> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPlaces();
        }
        String q = query.trim().toLowerCase();
        return placesMap.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(q)
                        || p.getCountry().toLowerCase().contains(q)
                        || (p.getStateOrRegion() != null && p.getStateOrRegion().toLowerCase().contains(q))
                        || p.getPrimaryCategory().getDisplayName().toLowerCase().contains(q)
                        || p.getTopAttractions().stream().anyMatch(a -> a.toLowerCase().contains(q))
                        || p.getActivities().stream().anyMatch(a -> a.toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }

    public List<Place> filter(Category category, BudgetTier budgetTier, Season season) {
        return placesMap.values().stream()
                .filter(p -> category == null || p.matchesCategory(category))
                .filter(p -> budgetTier == null || p.getBudgetTier() == budgetTier)
                .filter(p -> season == null || p.matchesSeason(season))
                .collect(Collectors.toList());
    }

    public void exportToCsv(File file) {
        List<String> lines = new ArrayList<>();
        // CSV Header
        lines.add("id,name,country,stateOrRegion,primaryCategory,budgetTier,estimatedDailyCostUSD,rating,minDays,maxDays,shortDescription,topAttractions,activities,bestSeasons,suitableCompanions,localDelicacies,travelTips");

        for (Place p : placesMap.values()) {
            List<String> cols = new ArrayList<>();
            cols.add(p.getId());
            cols.add(p.getName());
            cols.add(p.getCountry());
            cols.add(p.getStateOrRegion() != null ? p.getStateOrRegion() : "");
            cols.add(p.getPrimaryCategory().name());
            cols.add(p.getBudgetTier().name());
            cols.add(String.valueOf(p.getEstimatedDailyCostUSD()));
            cols.add(String.valueOf(p.getRating()));
            cols.add(String.valueOf(p.getMinRecommendedDays()));
            cols.add(String.valueOf(p.getMaxRecommendedDays()));
            cols.add(p.getShortDescription());
            cols.add(String.join(";", p.getTopAttractions()));
            cols.add(String.join(";", p.getActivities()));
            cols.add(p.getBestSeasons().stream().map(Enum::name).collect(Collectors.joining(";")));
            cols.add(p.getSuitableCompanions().stream().map(Enum::name).collect(Collectors.joining(";")));
            cols.add(String.join(";", p.getLocalDelicacies()));
            cols.add(p.getTravelTips() != null ? p.getTravelTips() : "");
            lines.add(FileStorageHelper.toCsvLine(cols));
        }

        try {
            FileStorageHelper.writeAllLines(file, lines);
        } catch (Exception e) {
            System.err.println("Failed to export places to CSV: " + e.getMessage());
        }
    }

    public void importFromCsv(File file) {
        if (!file.exists()) return;

        try {
            List<String> lines = FileStorageHelper.readAllLines(file);
            if (lines.size() <= 1) return; // Empty or just header

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;

                List<String> cols = FileStorageHelper.parseCsvLine(line);
                if (cols.size() < 11) continue;

                Place p = new Place();
                p.setId(cols.get(0));
                p.setName(cols.get(1));
                p.setCountry(cols.get(2));
                p.setStateOrRegion(cols.get(3));
                p.setPrimaryCategory(Category.fromString(cols.get(4)));
                p.setBudgetTier(BudgetTier.fromString(cols.get(5)));
                p.setEstimatedDailyCostUSD(Double.parseDouble(cols.get(6)));
                p.setRating(Double.parseDouble(cols.get(7)));
                p.setMinRecommendedDays(Integer.parseInt(cols.get(8)));
                p.setMaxRecommendedDays(Integer.parseInt(cols.get(9)));
                p.setShortDescription(cols.get(10));

                if (cols.size() > 11 && !cols.get(11).isEmpty()) {
                    for (String a : cols.get(11).split(";")) p.addTopAttraction(a);
                }
                if (cols.size() > 12 && !cols.get(12).isEmpty()) {
                    for (String act : cols.get(12).split(";")) p.addActivity(act);
                }
                if (cols.size() > 13 && !cols.get(13).isEmpty()) {
                    for (String s : cols.get(13).split(";")) {
                        Season season = Season.fromString(s);
                        if (season != null) p.addBestSeason(season);
                    }
                }
                if (cols.size() > 14 && !cols.get(14).isEmpty()) {
                    for (String c : cols.get(14).split(";")) {
                        CompanionType ct = CompanionType.fromString(c);
                        if (ct != null) p.addSuitableCompanion(ct);
                    }
                }
                if (cols.size() > 15 && !cols.get(15).isEmpty()) {
                    for (String d : cols.get(15).split(";")) p.addLocalDelicacy(d);
                }
                if (cols.size() > 16) {
                    p.setTravelTips(cols.get(16));
                }

                placesMap.put(p.getId(), p);
            }
        } catch (Exception e) {
            System.err.println("Failed to import places from CSV: " + e.getMessage());
        }
    }

    private void seedInitialDestinations() {
        // --- 1. GOA, INDIA ---
        Place goa = new Place("P001", "Goa", "India", "Goa", Category.BEACH, BudgetTier.BUDGET, 42.0, 4.7, 3, 6,
                "Sun-kissed coastline known for golden beaches, Portuguese heritage churches, lively beach shacks, and seafood.");
        goa.addSecondaryCategory(Category.HERITAGE);
        goa.addSecondaryCategory(Category.ADVENTURE);
        goa.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        goa.setSuitableCompanions(Arrays.asList(CompanionType.FRIENDS, CompanionType.COUPLE, CompanionType.SOLO));
        goa.setTopAttractions(Arrays.asList("Palolem Beach", "Baga Beach", "Basilica of Bom Jesus", "Fort Aguada", "Dudhsagar Falls"));
        goa.setActivities(Arrays.asList("Scuba Diving & Parasailing", "Sunset Cruise on Mandovi River", "Scooter Beach Hopping", "Night Markets"));
        goa.setLocalDelicacies(Arrays.asList("Goan Fish Curry & Rice", "Bebinca dessert", "Pork Vindaloo", "Feni cocktail"));
        goa.setTravelTips("Rent a scooter for easy exploration. Visit South Goa for serene relaxation and North Goa for nightlife.");
        goa.setSampleItinerary("Day 1: North Goa beaches (Anjuna, Baga) & sunset at Chapora Fort.\nDay 2: Water sports at Calangute & night market.\nDay 3: Old Goa churches & Latin Quarter (Fontainhas).\nDay 4: South Goa peaceful beaches (Palolem, Agonda) & seafood dinner.");
        addPlace(goa);

        // --- 2. MANALI, INDIA ---
        Place manali = new Place("P002", "Manali", "India", "Himachal Pradesh", Category.MOUNTAIN, BudgetTier.BUDGET, 38.0, 4.6, 4, 7,
                "Picturesque Himalayan hill town nestled by the Beas River, famed for snow-capped peaks, pine forests, and adventure sports.");
        manali.addSecondaryCategory(Category.ADVENTURE);
        manali.setBestSeasons(Arrays.asList(Season.SUMMER, Season.WINTER));
        manali.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FRIENDS, CompanionType.FAMILY, CompanionType.SOLO));
        manali.setTopAttractions(Arrays.asList("Solang Valley", "Rohtang Pass", "Hadimba Temple", "Old Manali Cafes", "Jogini Waterfall"));
        manali.setActivities(Arrays.asList("Paragliding in Solang", "Skiing & Snowboarding", "River Rafting in Kullu", "Trek to Jogini Waterfall"));
        manali.setLocalDelicacies(Arrays.asList("Siddu with Ghee", "Trout Fish Fry", "Thukpa & Momos", "Kullu Apple Crumble"));
        manali.setTravelTips("Carry warm layers even in summer. Book Rohtang Pass permits in advance online.");
        manali.setSampleItinerary("Day 1: Explore Mall Road, Hadimba Temple & Van Vihar.\nDay 2: Adventure sports in Solang Valley.\nDay 3: Day trip to Atal Tunnel and Sissu (Lahaul Valley).\nDay 4: Relax in Old Manali cafes and hike to Jogini Falls.");
        addPlace(manali);

        // --- 3. JAIPUR, INDIA ---
        Place jaipur = new Place("P003", "Jaipur", "India", "Rajasthan", Category.HERITAGE, BudgetTier.BUDGET, 45.0, 4.8, 3, 5,
                "The legendary Pink City of Rajasthan, boasting majestic hilltop forts, ornate palaces, colorful bazaars, and royal Rajput heritage.");
        jaipur.addSecondaryCategory(Category.CULTURAL);
        jaipur.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        jaipur.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.COUPLE, CompanionType.SOLO));
        jaipur.setTopAttractions(Arrays.asList("Amber Fort & Palace", "Hawa Mahal", "City Palace", "Jantar Mantar Observatory", "Nahargarh Fort"));
        jaipur.setActivities(Arrays.asList("Hot Air Balloon Safari", "Heritage Walk in Old Bazaars", "Sound & Light Show at Amber", "Block Printing Workshop"));
        jaipur.setLocalDelicacies(Arrays.asList("Dal Baati Churma", "Pyaaz Kachori", "Ghewar sweet", "Laal Maas"));
        jaipur.setTravelTips("Visit Amber Fort early morning to avoid crowds. Take composite entry tickets for big savings.");
        jaipur.setSampleItinerary("Day 1: Amber Fort, Jal Mahal, and sunset at Nahargarh Fort.\nDay 2: City Palace, Jantar Mantar, and photo op at Hawa Mahal.\nDay 3: Shopping in Johari Bazaar & cultural dinner at Chokhi Dhani.");
        addPlace(jaipur);

        // --- 4. BALI, INDONESIA ---
        Place bali = new Place("P004", "Bali", "Indonesia", "Bali Province", Category.BEACH, BudgetTier.MODERATE, 75.0, 4.8, 5, 10,
                "Tropical paradise combining iconic surf beaches, volcanic ridges, lush terraced rice fields, and tranquil spiritual Hindu temples.");
        bali.addSecondaryCategory(Category.CULTURAL);
        bali.addSecondaryCategory(Category.ADVENTURE);
        bali.setBestSeasons(Arrays.asList(Season.SUMMER, Season.SPRING, Season.AUTUMN));
        bali.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.SOLO, CompanionType.FRIENDS, CompanionType.FAMILY));
        bali.setTopAttractions(Arrays.asList("Uluwatu Temple & Cliffs", "Tegallalang Rice Terraces", "Mount Batur", "Nusa Penida Island", "Ubud Monkey Forest"));
        bali.setActivities(Arrays.asList("Sunrise Hike up Mount Batur", "Surfing in Canggu", "Balinese Spa & Yoga in Ubud", "Snorkeling with Manta Rays"));
        bali.setLocalDelicacies(Arrays.asList("Nasi Goreng", "Babi Guling", "Satay Lilit", "Fresh Young Coconut"));
        bali.setTravelTips("Split your stay between Ubud (culture & jungle) and Seminyak/Canggu (beaches & cafe life).");
        bali.setSampleItinerary("Day 1-2: Canggu/Seminyak beach clubs and Uluwatu sunset.\nDay 3-4: Ubud rice terraces, Sacred Monkey Forest & artisan markets.\nDay 5: Mount Batur sunrise hike & hot springs.\nDay 6: Day trip to Nusa Penida (Kelingking Beach).");
        addPlace(bali);

        // --- 5. SWISS ALPS (INTERLAKEN / ZERMATT), SWITZERLAND ---
        Place swissAlps = new Place("P005", "Swiss Alps", "Switzerland", "Bernese Oberland / Valais", Category.MOUNTAIN, BudgetTier.LUXURY, 240.0, 4.9, 4, 8,
                "Epic mountain wonderland of glacial peaks, mirror lakes, charming cogwheel alpine trains, and chocolate-box wooden chalets.");
        swissAlps.addSecondaryCategory(Category.ADVENTURE);
        swissAlps.setBestSeasons(Arrays.asList(Season.WINTER, Season.SUMMER));
        swissAlps.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.FRIENDS));
        swissAlps.setTopAttractions(Arrays.asList("Jungfraujoch (Top of Europe)", "Matterhorn & Zermatt", "Lauterbrunnen Valley", "Lake Brienz", "Grindelwald First"));
        swissAlps.setActivities(Arrays.asList("World-class Skiing & Snowboarding", "First Cliff Walk & Zip-rider", "Scenic Train Rides (Glacier Express)", "Hiking Alpine Trails"));
        swissAlps.setLocalDelicacies(Arrays.asList("Swiss Cheese Fondue", "Raclette", "Rösti with Bratwurst", "Swiss Artisan Chocolates"));
        swissAlps.setTravelTips("Invest in a Swiss Travel Pass for unlimited rides on scenic trains, boats, and mountain discounts.");
        swissAlps.setSampleItinerary("Day 1: Arrive Interlaken & cruise on Lake Brienz.\nDay 2: Day excursion to Jungfraujoch & Lauterbrunnen waterfalls.\nDay 3: Grindelwald First cliff walk and mountain carting.\nDay 4: Scenic train to Zermatt for sunset views of the Matterhorn.");
        addPlace(swissAlps);

        // --- 6. KYOTO, JAPAN ---
        Place kyoto = new Place("P006", "Kyoto", "Japan", "Kansai", Category.HERITAGE, BudgetTier.MODERATE, 130.0, 4.9, 3, 6,
                "Cultural heart of Japan with thousands of classical Buddhist temples, Zen gardens, bamboo groves, and traditional geisha districts.");
        kyoto.addSecondaryCategory(Category.CULTURAL);
        kyoto.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN));
        kyoto.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.SOLO, CompanionType.FAMILY));
        kyoto.setTopAttractions(Arrays.asList("Fushimi Inari Shrine (10,000 Torii gates)", "Kinkaku-ji (Golden Pavilion)", "Arashiyama Bamboo Grove", "Kiyomizu-dera Temple", "Gion Historic District"));
        kyoto.setActivities(Arrays.asList("Traditional Tea Ceremony", "Kimono Stroll through Gion", "Zen Meditation Workshop", "Cherry Blossom / Autumn Foliage Viewing"));
        kyoto.setLocalDelicacies(Arrays.asList("Kaiseki Multi-course Dining", "Matcha Parfait & Green Tea Sweets", "Kyoto Yudofu (Tofu hotpot)", "Ramen at Kyoto Station"));
        kyoto.setTravelTips("Visit Fushimi Inari and Arashiyama at sunrise (6:30 AM) to experience serene beauty without tourists.");
        kyoto.setSampleItinerary("Day 1: Arashiyama Bamboo Grove, Monkey Park & Tenryu-ji Temple.\nDay 2: Kinkaku-ji, Ryoan-ji rock garden, and Gion evening walk.\nDay 3: Early morning Fushimi Inari hike followed by Kiyomizu-dera.");
        addPlace(kyoto);

        // --- 7. MALDIVES ---
        Place maldives = new Place("P007", "Maldives", "Maldives", "Male Atolls", Category.BEACH, BudgetTier.LUXURY, 320.0, 4.9, 4, 7,
                "Ultimate luxury escape featuring overwater villas suspended over crystal-clear turquoise lagoons, coral atolls, and vibrant marine life.");
        maldives.addSecondaryCategory(Category.ADVENTURE);
        maldives.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        maldives.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE));
        maldives.setTopAttractions(Arrays.asList("Ari Atoll Coral Reefs", "Bioluminescent Beach (Vaadhoo Island)", "Male Fish Market", "Baa Atoll Biosphere"));
        maldives.setActivities(Arrays.asList("Overwater Villa Luxury Living", "Snorkeling with Whale Sharks", "Underwater Restaurant Dining", "Sunset Dolphin Cruises"));
        maldives.setLocalDelicacies(Arrays.asList("Garudhiya fish soup", "Mas Huni with Roshi", "Fresh Grilled Reef Fish", "Coconut sweets"));
        maldives.setTravelTips("All-inclusive resort packages provide the best peace of mind given high island import costs.");
        maldives.setSampleItinerary("Day 1: Seaplane arrival to private resort island.\nDay 2: Snorkeling house reef & sunset dolphin catamaran cruise.\nDay 3: Spa treatment and candlelight private beach dinner.\nDay 4: Coral planting & water sports before departure.");
        addPlace(maldives);

        // --- 8. RISHIKESH, INDIA ---
        Place rishikesh = new Place("P008", "Rishikesh", "India", "Uttarakhand", Category.ADVENTURE, BudgetTier.BUDGET, 32.0, 4.7, 3, 5,
                "Yoga Capital of the World on the holy Ganges River, offering world-class white water rafting, bungee jumping, and spiritual serenity.");
        rishikesh.addSecondaryCategory(Category.CULTURAL);
        rishikesh.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN, Season.WINTER));
        rishikesh.setSuitableCompanions(Arrays.asList(CompanionType.SOLO, CompanionType.FRIENDS, CompanionType.COUPLE));
        rishikesh.setTopAttractions(Arrays.asList("Ram Jhula & Laxman Jhula", "Triveni Ghat Evening Aarti", "Beatles Ashram (Chaurasi Kutia)", "Neer Garh Waterfall", "Parmarth Niketan"));
        rishikesh.setActivities(Arrays.asList("Grade III-IV White Water Rafting", "India's Highest Bungee Jump (83m)", "Sunrise Yoga & Meditation", "Riverside Camping"));
        rishikesh.setLocalDelicacies(Arrays.asList("Ayurvedic Organic Thali", "Aloo Puri at Chotiwala", "Masala Chai by the Ghats", "Fresh Fruit Smoothies"));
        rishikesh.setTravelTips("Avoid monsoon season (July-August) as river rafting is completely suspended.");
        rishikesh.setSampleItinerary("Day 1: Walk across suspension bridges, visit Beatles Ashram & Triveni Ghat Aarti.\nDay 2: 16km White Water Rafting expedition & cliff jumping.\nDay 3: Bungee jumping adventure at Mohan Chatti and evening meditation.");
        addPlace(rishikesh);

        // --- 9. SERENGETI NATIONAL PARK, TANZANIA ---
        Place serengeti = new Place("P009", "Serengeti National Park", "Tanzania", "Mara / Simiyu", Category.WILDLIFE, BudgetTier.LUXURY, 350.0, 5.0, 4, 8,
                "Earth's premier wildlife sanctuary, world-famous for the Great Migration of millions of wildebeest and the African 'Big Five'.");
        serengeti.addSecondaryCategory(Category.ADVENTURE);
        serengeti.setBestSeasons(Arrays.asList(Season.SUMMER, Season.AUTUMN, Season.WINTER));
        serengeti.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.SOLO));
        serengeti.setTopAttractions(Arrays.asList("Seronera Valley (Big Cats)", "Mara River Crossing", "Ngorongoro Crater", "Olduvai Gorge"));
        serengeti.setActivities(Arrays.asList("Hot Air Balloon Safari over Savannah", "4x4 Open-Roof Game Drives", "Bush Campfires & Stargazing", "Maasai Village Cultural Visit"));
        serengeti.setLocalDelicacies(Arrays.asList("Ugali with Braised Beef", "Nyama Choma BBQ", "Zanzibar Spice Rice", "African Safari High Tea"));
        serengeti.setTravelTips("Bring good binoculars and a zoom camera lens (300mm+). Pack neutral earthy-colored clothing.");
        serengeti.setSampleItinerary("Day 1: Fly into Seronera airstrip, afternoon game drive spotting lions & leopards.\nDay 2: Full-day safari tracking herds and predator interactions.\nDay 3: Sunrise hot air balloon safari followed by bush breakfast.\nDay 4: Ngorongoro crater rim game drive.");
        addPlace(serengeti);

        // --- 10. PARIS, FRANCE ---
        Place paris = new Place("P010", "Paris", "France", "Île-de-France", Category.METROPOLITAN, BudgetTier.MODERATE, 160.0, 4.8, 4, 7,
                "The City of Light, synonymous with timeless fashion, haute cuisine, iconic landmarks, world-famous art museums, and romantic boulevards.");
        paris.addSecondaryCategory(Category.HERITAGE);
        paris.addSecondaryCategory(Category.CULTURAL);
        paris.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN, Season.SUMMER));
        paris.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.SOLO, CompanionType.FAMILY, CompanionType.FRIENDS));
        paris.setTopAttractions(Arrays.asList("Eiffel Tower", "Louvre Museum", "Notre-Dame Cathedral", "Arc de Triomphe & Champs-Élysées", "Sacre-Cœur & Montmartre"));
        paris.setActivities(Arrays.asList("Seine River Sunset Cruise", "Masterpiece Hunt at the Louvre & Orsay", "Picnic at Champ de Mars", "Pastry & Wine Tasting in Le Marais"));
        paris.setLocalDelicacies(Arrays.asList("Warm Butter Croissants & Cafe au Lait", "Duck Confit", "French Crepes & Macarons", "Artisan Cheese & Baguette"));
        paris.setTravelTips("Use the Paris Metro for fast transit. Book Louvre and Eiffel Tower summit tickets weeks ahead.");
        paris.setSampleItinerary("Day 1: Eiffel Tower, Champ de Mars picnic & Seine river boat cruise.\nDay 2: Louvre Museum and stroll through Tuileries Garden to Champs-Élysées.\nDay 3: Montmartre artistic village, Sacre-Cœur basilica & Latin Quarter.\nDay 4: Day trip to Palace of Versailles.");
        addPlace(paris);

        // --- 11. LEH-LADAKH, INDIA ---
        Place ladakh = new Place("P011", "Leh-Ladakh", "India", "Ladakh", Category.MOUNTAIN, BudgetTier.MODERATE, 65.0, 4.9, 6, 10,
                "The Land of High Passes: dramatic high-altitude desert moonscapes, crystalline glacial lakes, ancient Tibetan Buddhist monasteries.");
        ladakh.addSecondaryCategory(Category.ADVENTURE);
        ladakh.addSecondaryCategory(Category.CULTURAL);
        ladakh.setBestSeasons(Arrays.asList(Season.SUMMER));
        ladakh.setSuitableCompanions(Arrays.asList(CompanionType.FRIENDS, CompanionType.SOLO, CompanionType.COUPLE));
        ladakh.setTopAttractions(Arrays.asList("Pangong Tso Lake", "Nubra Valley & Khardung La Pass", "Magnetic Hill", "Thiksey Monastery", "Shanti Stupa"));
        ladakh.setActivities(Arrays.asList("Motorcycle Expedition across Khardung La", "Camping under Milky Way at Pangong", "Double-humped Camel Ride in Hunder Dunes", "Monastery Chants"));
        ladakh.setLocalDelicacies(Arrays.asList("Ladakhi Butter Tea (Gur Gur)", "Skyu and Chhutagi pasta", "Tingmo with spicy gravy", "Tibetan Thukpa"));
        ladakh.setTravelTips("Mandatory 48-hour acclimatization in Leh on arrival to prevent Altitude Sickness (AMS). Drink plenty of water.");
        ladakh.setSampleItinerary("Day 1-2: Rest & acclimatize in Leh; visit Shanti Stupa and Leh Palace.\nDay 3: Drive over Khardung La to Nubra Valley; camp at Hunder.\nDay 4: Drive via Shyok to Pangong Tso Lake; stargazing.\nDay 5: Pangong sunrise & return via Chang La to Leh.");
        addPlace(ladakh);

        // --- 12. TOKYO, JAPAN ---
        Place tokyo = new Place("P012", "Tokyo", "Japan", "Kanto", Category.METROPOLITAN, BudgetTier.MODERATE, 140.0, 4.9, 4, 8,
                "Hyper-modern metropolis where neon-lit futuristic skyscrapers, robot cafes, and anime districts coexist with ancient Shinto shrines.");
        tokyo.addSecondaryCategory(Category.CULTURAL);
        tokyo.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN, Season.WINTER));
        tokyo.setSuitableCompanions(Arrays.asList(CompanionType.SOLO, CompanionType.FRIENDS, CompanionType.FAMILY, CompanionType.COUPLE));
        tokyo.setTopAttractions(Arrays.asList("Shibuya Crossing & Hachiko", "Senso-ji Temple in Asakusa", "Tokyo Skytree", "Akihabara Electric Town", "teamLab Borderless"));
        tokyo.setActivities(Arrays.asList("Walk across busy Shibuya scramble", "Interactive Digital Art at teamLab", "Tsukiji Outer Market Food Crawl", "Karaoke & Izakaya Crawl in Shinjuku"));
        tokyo.setLocalDelicacies(Arrays.asList("Edomae Sushi", "Tonkotsu Ramen", "Yakitori skewers", "Matcha soft serve & Wagyu beef"));
        tokyo.setTravelTips("Get a Suica/Pasmo IC transit card on your phone. Trains stop around midnight.");
        tokyo.setSampleItinerary("Day 1: Historic Asakusa (Senso-ji) & Tokyo Skytree panorama.\nDay 2: Meiji Jingu shrine, Harajuku fashion street & Shibuya crossing.\nDay 3: Digital art at teamLab & Ginza luxury shopping.\nDay 4: Akihabara anime hub & Shinjuku golden gai nightlife.");
        addPlace(tokyo);

        // --- 13. VARANASI, INDIA ---
        Place varanasi = new Place("P013", "Varanasi", "India", "Uttar Pradesh", Category.CULTURAL, BudgetTier.BUDGET, 28.0, 4.7, 2, 4,
                "One of the world's oldest continuously inhabited cities, the spiritual heart of Hinduism along the sacred banks of the Ganges.");
        varanasi.addSecondaryCategory(Category.HERITAGE);
        varanasi.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING, Season.AUTUMN));
        varanasi.setSuitableCompanions(Arrays.asList(CompanionType.SOLO, CompanionType.FAMILY, CompanionType.COUPLE));
        varanasi.setTopAttractions(Arrays.asList("Dashashwamedh Ghat", "Kashi Vishwanath Corridor", "Assi Ghat Sunrise", "Sarnath Buddhist Stupa", "Manikarnika Ghat"));
        varanasi.setActivities(Arrays.asList("Sunrise Boat Ride on the Ganga", "Witness Grand Evening Ganga Aarti", "Exploring narrow ancient alleyways", "Silk Saree Shopping"));
        varanasi.setLocalDelicacies(Arrays.asList("Banarasi Paan", "Kachori Jalebi breakfast", "Tamatar Chaat", "Malaiyo winter dessert"));
        varanasi.setTravelTips("Take a hand-rowed boat at dawn (5:30 AM) to see ghat rituals in ethereal golden light.");
        varanasi.setSampleItinerary("Day 1: Check in, walk Dashashwamedh Ghat & experience grand evening Aarti.\nDay 2: Dawn boat ride along 84 ghats, visit Kashi Vishwanath temple, and afternoon excursion to Sarnath.\nDay 3: Heritage lane walk, street food tour & Assi Ghat yoga.");
        addPlace(varanasi);

        // --- 14. QUEENSTOWN, NEW ZEALAND ---
        Place queenstown = new Place("P014", "Queenstown", "New Zealand", "Otago", Category.ADVENTURE, BudgetTier.LUXURY, 190.0, 4.9, 4, 8,
                "Adventure capital of the world set against the dramatic Remarkables mountain range and crystalline Lake Wakatipu.");
        queenstown.addSecondaryCategory(Category.MOUNTAIN);
        queenstown.setBestSeasons(Arrays.asList(Season.SUMMER, Season.WINTER));
        queenstown.setSuitableCompanions(Arrays.asList(CompanionType.FRIENDS, CompanionType.COUPLE, CompanionType.SOLO));
        queenstown.setTopAttractions(Arrays.asList("Milford Sound Fjord", "Skyline Gondola & Luge", "Kawarau Suspension Bridge", "Lake Wakatipu", "Coronet Peak"));
        queenstown.setActivities(Arrays.asList("Birthplace Bungee Jump at Kawarau", "Shotover Jet Boat Canyoning", "Skydiving over the Southern Alps", "Helicopter Glacier Tour"));
        queenstown.setLocalDelicacies(Arrays.asList("Fergburger gourmet burger", "Central Otago Pinot Noir", "New Zealand Lamb Roast", "Pavlova with Kiwifruit"));
        queenstown.setTravelTips("Milford Sound is a must-do day trip; book a coach-cruise-fly package for unbeatable aerial views.");
        queenstown.setSampleItinerary("Day 1: Skyline Gondola, Luge tracks & iconic Fergburger dinner.\nDay 2: Shotover Jet canyon ride & Kawarau bungee jump.\nDay 3: Full-day scenic tour to majestic Milford Sound.\nDay 4: Central Otago winery tour or winter skiing at Coronet Peak.");
        addPlace(queenstown);

        // --- 15. ROME, ITALY ---
        Place rome = new Place("P015", "Rome", "Italy", "Lazio", Category.HERITAGE, BudgetTier.MODERATE, 150.0, 4.8, 3, 6,
                "The Eternal City: an open-air museum filled with Roman ruins, renaissance fountains, the Colosseum, and the sacred Vatican City.");
        rome.addSecondaryCategory(Category.CULTURAL);
        rome.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN));
        rome.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.SOLO));
        rome.setTopAttractions(Arrays.asList("The Colosseum & Roman Forum", "Vatican Museums & Sistine Chapel", "Pantheon", "Trevi Fountain", "Piazza Navona"));
        rome.setActivities(Arrays.asList("Throwing coin in Trevi Fountain", "Guided Tour of Gladiator Arena", "Gelato hopping in Trastevere", "Authentic Pasta Making Class"));
        rome.setLocalDelicacies(Arrays.asList("Pasta Carbonara & Cacio e Pepe", "Roman Thin Crust Pizza", "Artisan Gelato", "Espresso at Sant'Eustachio"));
        rome.setTravelTips("Pre-book 'Skip-the-line' tickets for Vatican and Colosseum to save up to 3 hours of queueing.");
        rome.setSampleItinerary("Day 1: Colosseum, Roman Forum, and sunset at Palatine Hill.\nDay 2: Vatican Museums, St. Peter's Basilica, and Castel Sant'Angelo.\nDay 3: Pantheon, Trevi Fountain, Spanish Steps & evening in Trastevere.");
        addPlace(rome);

        // --- 16. JIM CORBETT NATIONAL PARK, INDIA ---
        Place corbett = new Place("P016", "Jim Corbett National Park", "India", "Uttarakhand", Category.WILDLIFE, BudgetTier.MODERATE, 55.0, 4.6, 3, 5,
                "India's oldest national park nestled in the Himalayan foothills, renowned for Royal Bengal Tigers, wild elephant herds, and dense sal forests.");
        corbett.addSecondaryCategory(Category.ADVENTURE);
        corbett.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        corbett.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.FRIENDS, CompanionType.COUPLE));
        corbett.setTopAttractions(Arrays.asList("Dhikala Forest Zone", "Bijrani Safari Zone", "Corbett Waterfall", "Kosi River", "Garjiya Devi Temple"));
        corbett.setActivities(Arrays.asList("Open Jeep Safari in Tiger Territory", "Night Stay inside Dhikala Forest Lodge", "Riverside Angling & Birdwatching", "Elephant spotting"));
        corbett.setLocalDelicacies(Arrays.asList("Kumaoni Dal & Bhatt ki Churkani", "Aloo ke Gutke", "Fresh River Fish Curry", "Bal Mithai"));
        corbett.setTravelTips("Book Dhikala zone safari permits 45 days in advance as slots are strictly capped for conservation.");
        corbett.setSampleItinerary("Day 1: Check into resort near Kosi River; visit Garjiya temple & evening nature walk.\nDay 2: Early morning Jeep Safari in Bijrani Zone; afternoon safari.\nDay 3: Forest safari to Corbett falls & bonfire dinner.");
        addPlace(corbett);

        // --- 17. DUBAI, UNITED ARAB EMIRATES ---
        Place dubai = new Place("P017", "Dubai", "United Arab Emirates", "Emirate of Dubai", Category.METROPOLITAN, BudgetTier.LUXURY, 220.0, 4.8, 4, 7,
                "Futuristic desert oasis boasting world records: tallest skyscraper, grandest luxury malls, artificial palm islands, and golden dune safaris.");
        dubai.addSecondaryCategory(Category.ADVENTURE);
        dubai.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        dubai.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.COUPLE, CompanionType.FRIENDS));
        dubai.setTopAttractions(Arrays.asList("Burj Khalifa (124th/148th floor)", "The Dubai Mall & Fountain Show", "Palm Jumeirah & Atlantis", "Dubai Marina", "Museum of the Future"));
        dubai.setActivities(Arrays.asList("4x4 Desert Dune Bashing with BBQ Dinner", "Burj Khalifa Observation Deck", "Yacht Cruise in Dubai Marina", "Aquaventure Waterpark"));
        dubai.setLocalDelicacies(Arrays.asList("Al Harees & Machboos", "Shawarma from Al Mallah", "Luqaimat sweet dumplings", "Arabic Coffee with Dates"));
        dubai.setTravelTips("Visit between November and March. Summer months (May-Sept) can reach 45°C+ outdoors.");
        dubai.setSampleItinerary("Day 1: Burj Khalifa summit, Dubai Mall & evening fountain show.\nDay 2: Palm Jumeirah, Aquaventure waterpark & Dubai Marina walk.\nDay 3: Afternoon Desert Safari with dune bashing, camel rides & Tanoura dance.\nDay 4: Museum of the Future & Old Dubai Gold Souk abra ride.");
        addPlace(dubai);

        // --- 18. ANDAMAN & NICOBAR ISLANDS, INDIA ---
        Place andaman = new Place("P018", "Andaman Islands", "India", "Andaman and Nicobar", Category.BEACH, BudgetTier.MODERATE, 70.0, 4.8, 5, 8,
                "Emerald archipelago in the Bay of Bengal, famous for pristine white-sand Radhanagar Beach, turquoise waters, and bioluminescence.");
        andaman.addSecondaryCategory(Category.ADVENTURE);
        andaman.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        andaman.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.FRIENDS));
        andaman.setTopAttractions(Arrays.asList("Radhanagar Beach (Havelock)", "Cellular Jail National Memorial", "Elephant Beach Coral Reef", "Neil Island (Bharatpur Beach)", "Ross Island"));
        andaman.setActivities(Arrays.asList("Sea Walking & Scuba Diving at Havelock", "Night Kayaking through Bioluminescent waters", "Glass-bottom boat ride", "Light & Sound Show at Cellular Jail"));
        andaman.setLocalDelicacies(Arrays.asList("Grilled Lobster & Crab", "Andaman Coconut Fish Curry", "Tandoori Prawns", "Tropical Fruit Platters"));
        andaman.setTravelTips("Book Makruzz or Nautika catamaran ferries between Port Blair and Havelock ahead of travel.");
        andaman.setSampleItinerary("Day 1: Port Blair: Cellular Jail and sound & light show.\nDay 2: Cruise ferry to Havelock Island; sunset at Radhanagar Beach.\nDay 3: Scuba diving and water activities at Elephant Beach.\nDay 4: Ferry to Neil Island; explore Natural Bridge.\nDay 5: Return to Port Blair.");
        addPlace(andaman);

        // --- 19. BANFF & LAKE LOUISE, CANADA ---
        Place banff = new Place("P019", "Banff National Park", "Canada", "Alberta", Category.MOUNTAIN, BudgetTier.MODERATE, 170.0, 4.9, 4, 7,
                "Crown jewel of the Canadian Rockies with electric turquoise glacial lakes, soaring jagged peaks, and abundant wildlife (grizzlies, elk).");
        banff.addSecondaryCategory(Category.ADVENTURE);
        banff.setBestSeasons(Arrays.asList(Season.SUMMER, Season.WINTER));
        banff.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.SOLO, CompanionType.FRIENDS));
        banff.setTopAttractions(Arrays.asList("Lake Louise", "Moraine Lake (Valley of the Ten Peaks)", "Banff Gondola (Sulphur Mountain)", "Icefields Parkway", "Johnston Canyon"));
        banff.setActivities(Arrays.asList("Canoeing on iridescent turquoise lake waters", "Ice walk through frozen waterfalls", "Scenic drive on Icefields Parkway", "Soaking in Banff Upper Hot Springs"));
        banff.setLocalDelicacies(Arrays.asList("Alberta AAA Beef Steak", "Canadian Poutine", "Maple Glazed Salmon", "BeaverTails pastry"));
        banff.setTravelTips("Moraine Lake road is closed to private cars; book the Parks Canada shuttle shuttle well in advance.");
        banff.setSampleItinerary("Day 1: Banff townsite, Sulphur Mountain Gondola & Upper Hot Springs.\nDay 2: Lake Louise & canoe rental; hike to Plain of Six Glaciers Tea House.\nDay 3: Shuttle to Moraine Lake; hike Johnston Canyon waterfalls.\nDay 4: Drive Icefields Parkway and Columbia Icefield skywalk.");
        addPlace(banff);

        // --- 20. AMALFI COAST, ITALY ---
        Place amalfi = new Place("P020", "Amalfi Coast", "Italy", "Campania", Category.BEACH, BudgetTier.LUXURY, 280.0, 4.8, 4, 7,
                "Breathtaking vertical coastline where colorful pastel villages cling to sheer sea cliffs overlooking azure Mediterranean waters.");
        amalfi.addSecondaryCategory(Category.HERITAGE);
        amalfi.setBestSeasons(Arrays.asList(Season.SPRING, Season.SUMMER, Season.AUTUMN));
        amalfi.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FRIENDS));
        amalfi.setTopAttractions(Arrays.asList("Positano Cliffside Village", "Amalfi Cathedral (Duomo)", "Ravello Cliff Gardens (Villa Rufolo)", "Capri Island & Blue Grotto", "Path of the Gods"));
        amalfi.setActivities(Arrays.asList("Private Sunset Yacht Charter to Capri", "Hiking the scenic Path of the Gods", "Limoncello Tasting at lemon groves", "Cliffside dining with sea panorama"));
        amalfi.setLocalDelicacies(Arrays.asList("Limoncello liqueur", "Spaghetti alle Vongole (Clams)", "Fresh Mozzarella di Bufala", "Delizia al Limone cake"));
        amalfi.setTravelTips("Take public ferries rather than driving narrow cliff roads, which often suffer heavy traffic jams.");
        amalfi.setSampleItinerary("Day 1: Settle in Positano, browse boutique lanes & beachside dinner.\nDay 2: Boat excursion to Capri Island and the Blue Grotto.\nDay 3: Visit Amalfi town and high-altitude clifftop gardens of Ravello.\nDay 4: Morning hike on Path of the Gods.");
        addPlace(amalfi);

        // --- 21. COSTA RICA (ARENAL & MANUEL ANTONIO) ---
        Place costaRica = new Place("P021", "Costa Rica", "Costa Rica", "Alajuela / Puntarenas", Category.ADVENTURE, BudgetTier.MODERATE, 95.0, 4.8, 5, 9,
                "World's biodiversity epicenter, famous for active Arenal volcano, thermal hot springs, cloud forest canopy zip-lines, and sloths.");
        costaRica.addSecondaryCategory(Category.WILDLIFE);
        costaRica.setBestSeasons(Arrays.asList(Season.WINTER, Season.SPRING));
        costaRica.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.COUPLE, CompanionType.FRIENDS, CompanionType.SOLO));
        costaRica.setTopAttractions(Arrays.asList("Arenal Volcano National Park", "Manuel Antonio National Park", "Monteverde Cloud Forest", "Tabacon Hot Springs", "La Fortuna Waterfall"));
        costaRica.setActivities(Arrays.asList("Canopy Zip-lining above Rain Forest", "Soaking in Volcanic Thermal Springs", "Night Wildlife Walk spotting frogs & sloths", "Surfing in Pacific Coast"));
        costaRica.setLocalDelicacies(Arrays.asList("Gallo Pinto breakfast", "Casado mixed plate", "Fresh Ceviche", "Highland Costa Rican Coffee"));
        costaRica.setTravelTips("Embrace the 'Pura Vida' slow-travel mindset and rent a 4x4 SUV for gravel mountain roads.");
        costaRica.setSampleItinerary("Day 1-2: La Fortuna: Arenal volcano hike, waterfall swim & Tabacon hot springs.\nDay 3: Monteverde hanging bridges canopy walk.\nDay 4-5: Manuel Antonio beaches, coastal rainforest wildlife spotting.");
        addPlace(costaRica);

        // --- 22. OOTY & COONOOR, INDIA ---
        Place ooty = new Place("P022", "Ooty", "India", "Tamil Nadu", Category.MOUNTAIN, BudgetTier.BUDGET, 35.0, 4.5, 3, 5,
                "Queen of Hill Stations in the Nilgiri Hills, famous for rolling green tea plantations, British heritage, and the UNESCO toy train.");
        ooty.addSecondaryCategory(Category.HERITAGE);
        ooty.setBestSeasons(Arrays.asList(Season.SPRING, Season.SUMMER, Season.WINTER));
        ooty.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.COUPLE, CompanionType.SOLO));
        ooty.setTopAttractions(Arrays.asList("Nilgiri Mountain Railway (Toy Train)", "Ooty Botanical Gardens", "Doddabetta Peak", "Pykara Lake & Waterfalls", "Coonoor Tea Gardens"));
        ooty.setActivities(Arrays.asList("Riding the UNESCO heritage steam train", "Row boating on Ooty Lake", "Tea Tasting at Factory", "Strolling in Government Rose Garden"));
        ooty.setLocalDelicacies(Arrays.asList("Homemade Nilgiri Chocolates", "Fresh Tea & Varkey biscuits", "South Indian Filter Coffee", "Avial & Dosa"));
        ooty.setTravelTips("Book the Toy Train tickets months in advance via IRCTC for the best scenic windows.");
        ooty.setSampleItinerary("Day 1: Ooty Lake, Rose Garden & Botanical Garden.\nDay 2: Toy train ride to Coonoor; visit Sim's Park and tea estates.\nDay 3: Panoramic view from Doddabetta peak & boating at Pykara lake.");
        addPlace(ooty);

        // --- 23. SINGAPORE ---
        Place singapore = new Place("P023", "Singapore", "Singapore", "Central Region", Category.METROPOLITAN, BudgetTier.MODERATE, 150.0, 4.8, 3, 5,
                "The Garden City: a futuristic blend of green bio-architecture, ultra-clean streets, Michelin hawker centers, and Sentosa island.");
        singapore.addSecondaryCategory(Category.CULTURAL);
        singapore.setBestSeasons(Arrays.asList(Season.ALL_YEAR));
        singapore.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.COUPLE, CompanionType.SOLO, CompanionType.FRIENDS));
        singapore.setTopAttractions(Arrays.asList("Gardens by the Bay & Supertree Grove", "Marina Bay Sands SkyPark", "Jewel Changi Airport Vortex", "Sentosa Island & Universal Studios", "Chinatown & Little India"));
        singapore.setActivities(Arrays.asList("Watch Garden Rhapsody Light Show", "Hawker Food Trail at Maxwell & Chinatown", "Night Safari tram ride", "Skyline cocktail at Marina Bay Sands"));
        singapore.setLocalDelicacies(Arrays.asList("Hainanese Chicken Rice", "Chili Crab with Fried Mantou", "Laksa Noodle Soup", "Kaya Toast with Soft Boiled Eggs"));
        singapore.setTravelTips("Tap in and out on the MRT subway with your contactless credit or debit card directly.");
        singapore.setSampleItinerary("Day 1: Marina Bay Sands, Merlion, and evening light show at Gardens by the Bay.\nDay 2: Chinatown heritage, Little India & Maxwell Food Centre.\nDay 3: Day trip to Sentosa Island & Universal Studios.\nDay 4: Singapore Botanic Gardens and Jewel Changi indoor waterfall.");
        addPlace(singapore);

        // --- 24. KERALA BACKWATERS (ALLEPPEY), INDIA ---
        Place kerala = new Place("P024", "Kerala Backwaters", "India", "Kerala", Category.CULTURAL, BudgetTier.BUDGET, 45.0, 4.8, 3, 5,
                "God's Own Country: tranquil maze of palm-fringed canals, serene lagoons, traditional thatched houseboats, and ayurvedic wellness.");
        kerala.addSecondaryCategory(Category.BEACH);
        kerala.setBestSeasons(Arrays.asList(Season.WINTER, Season.AUTUMN));
        kerala.setSuitableCompanions(Arrays.asList(CompanionType.COUPLE, CompanionType.FAMILY, CompanionType.SOLO));
        kerala.setTopAttractions(Arrays.asList("Vembanad Lake", "Marari Beach", "Kuttanad Rice Bowl", "Pathiramanal Bird Island", "Krishnapuram Palace"));
        kerala.setActivities(Arrays.asList("Overnight Deluxe Houseboat Cruise", "Traditional Ayurvedic Oil Massage", "Village Canoe Canal Tour", "Kathakali Classical Dance Show"));
        kerala.setLocalDelicacies(Arrays.asList("Karimeen Pollichathu (Pearl Spot Fish)", "Appam with Stew", "Kerala Banana Fritters (Pazham Pori)", "Traditional Sadya on Banana Leaf"));
        kerala.setTravelTips("Choose an overnight houseboat stay with onboard chef to taste freshly caught river fish.");
        kerala.setSampleItinerary("Day 1: Check into houseboat at Alleppey; cruise serene canals through emerald paddies.\nDay 2: Village canoe ride, visit local coir factory and ayurvedic spa.\nDay 3: Relax at tranquil Marari beach and enjoy sunset seafood.");
        addPlace(kerala);

        // --- 25. GRAND CANYON, USA ---
        Place grandCanyon = new Place("P025", "Grand Canyon", "United States", "Arizona", Category.ADVENTURE, BudgetTier.MODERATE, 130.0, 4.9, 2, 4,
                "One of the Seven Natural Wonders of the World: immense canyon carved by the Colorado River, boasting crimson geological strata.");
        grandCanyon.addSecondaryCategory(Category.MOUNTAIN);
        grandCanyon.setBestSeasons(Arrays.asList(Season.SPRING, Season.AUTUMN, Season.SUMMER));
        grandCanyon.setSuitableCompanions(Arrays.asList(CompanionType.FAMILY, CompanionType.FRIENDS, CompanionType.COUPLE, CompanionType.SOLO));
        grandCanyon.setTopAttractions(Arrays.asList("South Rim Mather Point", "Bright Angel Trail", "Desert View Watchtower", "Horseshoe Bend & Antelope Canyon nearby"));
        grandCanyon.setActivities(Arrays.asList("Helicopter flight over canyon abyss", "Hiking down Bright Angel Trail", "Sunrise and Sunset viewing at Hopi Point", "Colorado River Rafting"));
        grandCanyon.setLocalDelicacies(Arrays.asList("Navajo Fry Bread & Taco", "Southwestern Smoked Chili", "Prickly Pear Lemonade", "Mesquite Grilled Steak"));
        grandCanyon.setTravelTips("Stay inside the park at South Rim Village to avoid 1-2 hour entry gate traffic at sunrise.");
        grandCanyon.setSampleItinerary("Day 1: Rim trail walk, Mather Point and sunset at Hopi Point.\nDay 2: Early morning hike down Bright Angel Trail to 1.5-mile Resthouse.\nDay 3: Helicopter tour & scenic drive to Desert View Watchtower.");
        addPlace(grandCanyon);
    }
}
