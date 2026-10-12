package com.travelsync.model;

import java.util.ArrayList;
import java.util.List;

public class Itinerary {
    private List<Destination> orderedDestinations;
    private List<Route> legs;
    private double totalDistanceMeters;
    private double totalDurationSeconds;
    private String formattedTotalDistance;
    private String formattedTotalDuration;
    private String algorithmUsed;
    private long executionTimeNanos;
    private String complexity;

    public Itinerary() {
        this.orderedDestinations = new ArrayList<>();
        this.legs = new ArrayList<>();
    }

    public List<Destination> getOrderedDestinations() { return orderedDestinations; }
    public void setOrderedDestinations(List<Destination> orderedDestinations) { this.orderedDestinations = orderedDestinations; }

    public List<Route> getLegs() { return legs; }
    public void setLegs(List<Route> legs) { this.legs = legs; }

    public double getTotalDistanceMeters() { return totalDistanceMeters; }
    public void setTotalDistanceMeters(double totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
        this.formattedTotalDistance = String.format("%.1f km", totalDistanceMeters / 1000.0);
    }

    public double getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(double totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
        long hours = (long) totalDurationSeconds / 3600;
        long minutes = ((long) totalDurationSeconds % 3600) / 60;
        this.formattedTotalDuration = hours > 0 ? String.format("%d h %d m", hours, minutes) : String.format("%d mins", minutes);
    }

    public String getFormattedTotalDistance() { return formattedTotalDistance; }
    public void setFormattedTotalDistance(String formattedTotalDistance) { this.formattedTotalDistance = formattedTotalDistance; }

    public String getFormattedTotalDuration() { return formattedTotalDuration; }
    public void setFormattedTotalDuration(String formattedTotalDuration) { this.formattedTotalDuration = formattedTotalDuration; }

    public String getAlgorithmUsed() { return algorithmUsed; }
    public void setAlgorithmUsed(String algorithmUsed) { this.algorithmUsed = algorithmUsed; }

    public long getExecutionTimeNanos() { return executionTimeNanos; }
    public void setExecutionTimeNanos(long executionTimeNanos) { this.executionTimeNanos = executionTimeNanos; }

    public String getComplexity() { return complexity; }
    public void setComplexity(String complexity) { this.complexity = complexity; }
}
