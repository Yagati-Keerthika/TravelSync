package com.travelsync.dto;

import java.util.List;

public record ActivityAllocationRequest(
        List<Traveler> travelers,
        List<Activity> activities
) {
    public record Traveler(String id, String name, List<String> interests) {}

    public record Activity(String id, String name, List<String> categories, int capacity) {}
}
