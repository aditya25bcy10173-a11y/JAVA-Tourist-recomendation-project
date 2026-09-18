package com.tourist.repository;

import com.tourist.model.Place;
import com.tourist.util.FileStorageHelper;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages user's saved wishlist destinations and personal trip notes,
 * persisting them to a local file.
 */
public class WishlistRepository {

    public static class WishlistItem {
        private final Place place;
        private String notes;
        private final LocalDate addedDate;
        private String targetSeasonOrMonth;

        public WishlistItem(Place place, String notes, String targetSeasonOrMonth) {
            this.place = place;
            this.notes = notes != null ? notes : "";
            this.addedDate = LocalDate.now();
            this.targetSeasonOrMonth = targetSeasonOrMonth != null ? targetSeasonOrMonth : "Flexible";
        }

        public Place getPlace() { return place; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDate getAddedDate() { return addedDate; }
        public String getTargetSeasonOrMonth() { return targetSeasonOrMonth; }
        public void setTargetSeasonOrMonth(String targetSeasonOrMonth) { this.targetSeasonOrMonth = targetSeasonOrMonth; }
    }

    private final Map<String, WishlistItem> wishlistMap = new LinkedHashMap<>();
    private final File storageFile;
    private final PlaceRepository placeRepository;

    public WishlistRepository(File storageFile, PlaceRepository placeRepo) {
        this.storageFile = storageFile;
        this.placeRepository = placeRepo;
        loadWishlist();
    }

    public synchronized boolean add(Place place, String notes, String targetSeasonOrMonth) {
        if (place == null) return false;
        wishlistMap.put(place.getId(), new WishlistItem(place, notes, targetSeasonOrMonth));
        saveWishlist();
        return true;
    }

    public synchronized boolean remove(String placeId) {
        if (wishlistMap.remove(placeId) != null) {
            saveWishlist();
            return true;
        }
        return false;
    }

    public synchronized boolean contains(String placeId) {
        return wishlistMap.containsKey(placeId);
    }

    public synchronized List<WishlistItem> getAll() {
        return new ArrayList<>(wishlistMap.values());
    }

    public synchronized int size() {
        return wishlistMap.size();
    }

    public synchronized String exportSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           MY TRAVEL WISHLIST & ITINERARY           \n");
        sb.append("====================================================\n\n");

        if (wishlistMap.isEmpty()) {
            sb.append("Your wishlist is currently empty! Add places from the Suggestion or Explore tabs.\n");
            return sb.toString();
        }

        int count = 1;
        for (WishlistItem item : wishlistMap.values()) {
            Place p = item.getPlace();
            sb.append(String.format("%d. %s (%s)\n", count++, p.getName(), p.getFullLocation()));
            sb.append(String.format("   Theme: %s | Budget Level: %s (~$%.0f/day) | Rating: %.1f★\n",
                    p.getPrimaryCategory().getDisplayName(), p.getBudgetTier().getLabel(), p.getEstimatedDailyCostUSD(), p.getRating()));
            sb.append(String.format("   Ideal Duration: %d-%d days | Best Seasons: %s\n",
                    p.getMinRecommendedDays(), p.getMaxRecommendedDays(), p.getBestSeasons()));
            sb.append(String.format("   Key Sights: %s\n", String.join(", ", p.getTopAttractions())));
            if (!item.getNotes().isEmpty()) {
                sb.append(String.format("   Personal Notes: %s\n", item.getNotes()));
            }
            if (!"Flexible".equalsIgnoreCase(item.getTargetSeasonOrMonth())) {
                sb.append(String.format("   Target Travel Time: %s\n", item.getTargetSeasonOrMonth()));
            }
            sb.append("----------------------------------------------------\n");
        }
        return sb.toString();
    }

    private void saveWishlist() {
        if (storageFile == null) return;
        List<String> lines = new ArrayList<>();
        for (WishlistItem item : wishlistMap.values()) {
            lines.add(item.getPlace().getId() + "|||" + item.getTargetSeasonOrMonth() + "|||" + item.getNotes().replace("\n", " "));
        }
        try {
            FileStorageHelper.writeAllLines(storageFile, lines);
        } catch (Exception e) {
            System.err.println("Failed to save wishlist: " + e.getMessage());
        }
    }

    private void loadWishlist() {
        if (storageFile == null || !storageFile.exists()) return;
        try {
            List<String> lines = FileStorageHelper.readAllLines(storageFile);
            for (String line : lines) {
                String[] parts = line.split("\\|\\|\\|");
                if (parts.length >= 1) {
                    String placeId = parts[0].trim();
                    Place place = placeRepository.getById(placeId);
                    if (place != null) {
                        String targetTime = parts.length > 1 ? parts[1].trim() : "Flexible";
                        String notes = parts.length > 2 ? parts[2].trim() : "";
                        wishlistMap.put(placeId, new WishlistItem(place, notes, targetTime));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load wishlist: " + e.getMessage());
        }
    }
}
