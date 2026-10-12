package com.travelsync.service;

import com.travelsync.client.GeocodingApiClient;
import com.travelsync.client.OpenMeteoWeatherClient;
import com.travelsync.client.PlacesApiClient;
import com.travelsync.client.TravelInfoApiClient;
import com.travelsync.exception.ResourceNotFoundException;
import com.travelsync.model.Destination;
import com.travelsync.model.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DestinationService {

    private static final Logger log = LoggerFactory.getLogger(DestinationService.class);

    private final GeocodingApiClient geocodingClient;
    private final TravelInfoApiClient travelInfoClient;
    private final PlacesApiClient placesClient;
    private final OpenMeteoWeatherClient weatherClient;

    public DestinationService(GeocodingApiClient geocodingClient,
                              TravelInfoApiClient travelInfoClient,
                              PlacesApiClient placesClient,
                              OpenMeteoWeatherClient weatherClient) {
        this.geocodingClient = geocodingClient;
        this.travelInfoClient = travelInfoClient;
        this.placesClient = placesClient;
        this.weatherClient = weatherClient;
    }

    public List<Destination> searchDestinations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Destination> candidates = geocodingClient.searchDestinations(query.trim());
        if (candidates.isEmpty()) {
            return List.of();
        }

        // Enrich the top 3 results with encyclopedic summaries and weather
        int countToEnrich = Math.min(candidates.size(), 3);
        for (int i = 0; i < countToEnrich; i++) {
            Destination dest = candidates.get(i);
            travelInfoClient.enrichDestination(dest);
            try {
                dest.setWeather(weatherClient.getWeather(dest.getLatitude(), dest.getLongitude()));
            } catch (Exception e) {
                log.warn("Weather lookup failed for {}: {}", dest.getName(), e.getMessage());
            }
        }

        return candidates;
    }

    public Destination getDestinationByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination name cannot be blank");
        }

        List<Destination> candidates = geocodingClient.searchDestinations(name.trim());
        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("No destination found matching: " + name);
        }

        Destination destination = candidates.get(0);
        travelInfoClient.enrichDestination(destination);

        // Fetch live attractions
        try {
            List<Place> places = placesClient.getNearbyPlaces(destination.getLatitude(), destination.getLongitude(), 15000);
            destination.setPlaces(places);
        } catch (Exception ex) {
            log.warn("Failed to fetch places for {}: {}", destination.getName(), ex.getMessage());
        }

        // Fetch live weather
        try {
            destination.setWeather(weatherClient.getWeather(destination.getLatitude(), destination.getLongitude()));
        } catch (Exception ex) {
            log.warn("Failed to fetch weather for {}: {}", destination.getName(), ex.getMessage());
        }

        return destination;
    }

    public List<Place> getPlacesForDestination(String name) {
        Destination dest = getDestinationByName(name);
        return placesClient.getNearbyPlaces(dest.getLatitude(), dest.getLongitude(), 15000);
    }

    public List<Place> getPlacesNear(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordinates are outside valid latitude/longitude ranges");
        }
        return placesClient.getNearbyPlaces(latitude, longitude, 15000);
    }
}
