package com.travelsync.dto;

import java.util.List;

public class RecommendationRequest {
    private List<String> preferences; // e.g. ["beach", "historical", "adventure"]

    public RecommendationRequest() {}

    public RecommendationRequest(List<String> preferences) {
        this.preferences = preferences;
    }

    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
}
