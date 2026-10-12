package com.travelsync.model;

public class Route {
    private String origin;
    private String destination;
    private double distanceMeters;
    private double durationSeconds;
    private String formattedDistance;
    private String formattedDuration;

    public Route() {}

    public Route(String origin, String destination, double distanceMeters, double durationSeconds) {
        this.origin = origin;
        this.destination = destination;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.formattedDistance = String.format("%.1f km", distanceMeters / 1000.0);
        long hours = (long) durationSeconds / 3600;
        long minutes = ((long) durationSeconds % 3600) / 60;
        this.formattedDuration = hours > 0 ? String.format("%d h %d m", hours, minutes) : String.format("%d mins", minutes);
    }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(double durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getFormattedDistance() { return formattedDistance; }
    public void setFormattedDistance(String formattedDistance) { this.formattedDistance = formattedDistance; }

    public String getFormattedDuration() { return formattedDuration; }
    public void setFormattedDuration(String formattedDuration) { this.formattedDuration = formattedDuration; }
}
