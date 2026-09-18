package com.tourist.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a suggested Place along with its calculated compatibility score (0-100%)
 * and human-readable explanation factors.
 */
public class RecommendationResult implements Comparable<RecommendationResult> {
    private final Place place;
    private final double matchPercentage;
    private final List<String> matchReasons;
    private final List<String> cautions;

    public RecommendationResult(Place place, double matchPercentage) {
        this.place = place;
        this.matchPercentage = matchPercentage;
        this.matchReasons = new ArrayList<>();
        this.cautions = new ArrayList<>();
    }

    public Place getPlace() {
        return place;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public int getRoundedScore() {
        return (int) Math.round(matchPercentage);
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void addMatchReason(String reason) {
        if (reason != null && !reason.trim().isEmpty()) {
            this.matchReasons.add(reason);
        }
    }

    public List<String> getCautions() {
        return cautions;
    }

    public void addCaution(String caution) {
        if (caution != null && !caution.trim().isEmpty()) {
            this.cautions.add(caution);
        }
    }

    @Override
    public int compareTo(RecommendationResult other) {
        // Sort descending by match percentage, then by place rating
        int scoreCompare = Double.compare(other.matchPercentage, this.matchPercentage);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return Double.compare(other.place.getRating(), this.place.getRating());
    }

    @Override
    public String toString() {
        return String.format("[%d%% Match] %s - %s", getRoundedScore(), place.getName(), place.getFullLocation());
    }
}
