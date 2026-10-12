package com.travelsync.dto;

import java.util.List;

public class ItineraryOptimizeRequest {
    private List<String> destinations;
    private String objective; // "DISTANCE" or "DURATION"
    private int startIndex = 0;

    public ItineraryOptimizeRequest() {}

    public ItineraryOptimizeRequest(List<String> destinations, String objective) {
        this.destinations = destinations;
        this.objective = objective;
    }

    public List<String> getDestinations() { return destinations; }
    public void setDestinations(List<String> destinations) { this.destinations = destinations; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
}
