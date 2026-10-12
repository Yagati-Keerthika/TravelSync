package com.travelsync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.exception.ExternalApiException;
import com.travelsync.model.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoGeocodingClient implements GeocodingApiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoGeocodingClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${travelsync.api.geocoding.openmeteo-url:https://geocoding-api.open-meteo.com/v1/search}")
    private String geocodingUrl;

    public OpenMeteoGeocodingClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Destination> searchDestinations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("name", query.trim())
                .queryParam("count", 10)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .toUriString();

        try {
            log.info("Querying live Open-Meteo Geocoding API for '{}': {}", query, uri);
            String response = restTemplate.getForObject(uri, String.class);
            if (response == null || response.isBlank()) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");
            if (!results.isArray()) {
                return List.of();
            }

            List<Destination> destinations = new ArrayList<>();
            for (JsonNode item : results) {
                Destination dest = new Destination();
                dest.setName(item.path("name").asText(""));
                dest.setCountry(item.path("country").asText(""));
                dest.setCountryCode(item.path("country_code").asText(""));
                dest.setRegion(item.path("admin1").asText(""));
                dest.setLatitude(item.path("latitude").asDouble(0.0));
                dest.setLongitude(item.path("longitude").asDouble(0.0));
                dest.setTimezone(item.path("timezone").asText(""));
                if (item.has("population")) {
                    dest.setPopulation(item.path("population").asLong());
                }
                if (item.has("elevation")) {
                    dest.setElevation(item.path("elevation").asDouble());
                }
                destinations.add(dest);
            }
            log.info("Found {} live destination candidates for '{}'", destinations.size(), query);
            return destinations;
        } catch (Exception ex) {
            log.error("Failed to query Open-Meteo Geocoding API: {}", ex.getMessage());
            throw new ExternalApiException("Open-Meteo Geocoding", ex.getMessage(), ex);
        }
    }
}
