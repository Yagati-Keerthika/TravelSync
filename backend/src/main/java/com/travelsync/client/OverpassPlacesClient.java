package com.travelsync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.model.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class OverpassPlacesClient implements PlacesApiClient {

    private static final Logger log = LoggerFactory.getLogger(OverpassPlacesClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${travelsync.api.geocoding.nominatim-url:https://nominatim.openstreetmap.org/search}")
    private String nominatimUrl;

    private long lastNominatimRequestMillis;

    public OverpassPlacesClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Place> getNearbyPlaces(double latitude, double longitude, int radiusMeters) {
        List<Place> places = new ArrayList<>(searchNominatim("attraction", "Attraction", latitude, longitude, radiusMeters));
        places.addAll(searchNominatim("hotel", "Hotel", latitude, longitude, radiusMeters));
        places.sort(java.util.Comparator.comparingDouble(Place::getDistanceKm));
        return places;
    }

    private synchronized List<Place> searchNominatim(String query, String category, double latitude, double longitude, int radiusMeters) {
        long waitMillis = 1000 - (System.currentTimeMillis() - lastNominatimRequestMillis);
        if (waitMillis > 0) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while respecting Nominatim's request interval");
                return List.of();
            }
        }

        double latitudeDelta = radiusMeters / 111_320.0;
        double longitudeDelta = radiusMeters / (111_320.0 * Math.max(0.01, Math.cos(Math.toRadians(latitude))));
        String viewbox = String.format(java.util.Locale.US, "%.6f,%.6f,%.6f,%.6f",
                longitude - longitudeDelta, latitude + latitudeDelta,
                longitude + longitudeDelta, latitude - latitudeDelta);
        URI uri = UriComponentsBuilder.fromHttpUrl(nominatimUrl)
                .queryParam("format", "jsonv2")
                .queryParam("q", query)
                .queryParam("viewbox", viewbox)
                .queryParam("bounded", 1)
                .queryParam("limit", 10)
                .build()
                .toUri();

        try {
            lastNominatimRequestMillis = System.currentTimeMillis();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelSync/1.0 (academic travel information application)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode results = objectMapper.readTree(response.getBody());
                if (results.isArray()) {
                    List<Place> places = new ArrayList<>();
                    for (JsonNode item : results) {
                        String name = item.path("name").asText("");
                        if (name.isBlank()) {
                            continue;
                        }
                        double placeLatitude = item.path("lat").asDouble(0.0);
                        double placeLongitude = item.path("lon").asDouble(0.0);
                        double distanceKm = haversineKm(latitude, longitude, placeLatitude, placeLongitude);
                        if (distanceKm * 1000 > radiusMeters) {
                            continue;
                        }
                        Place place = new Place();
                        place.setName(name);
                        place.setCategory(category);
                        place.setLatitude(placeLatitude);
                        place.setLongitude(placeLongitude);
                        place.setDistanceKm(distanceKm);
                        place.setDescription(item.path("display_name").asText(""));
                        place.setOsmType(item.path("osm_type").asText(""));
                        place.setOsmId(item.path("osm_id").asLong());
                        places.add(place);
                    }
                    log.info("Found {} nearby {} results for coordinates ({}, {})", places.size(), query, latitude, longitude);
                    return places;
                }
            }
        } catch (Exception ex) {
            log.warn("Could not retrieve nearby {} results for ({}, {}): {}", query, latitude, longitude, ex.getMessage());
        }

        return List.of();
    }

    private double haversineKm(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

}
