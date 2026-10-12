package com.travelsync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.model.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class WikipediaTravelInfoClient implements TravelInfoApiClient {

    private static final Logger log = LoggerFactory.getLogger(WikipediaTravelInfoClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${travelsync.api.wikipedia.summary-url:https://en.wikipedia.org/api/rest_v1/page/summary}")
    private String summaryBaseUrl;

    public WikipediaTravelInfoClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Destination enrichDestination(Destination destination) {
        if (destination == null || destination.getName() == null) {
            return destination;
        }

        String searchTitle = destination.getName().trim().replace(" ", "_");
        String url = summaryBaseUrl + "/" + URLEncoder.encode(searchTitle, StandardCharsets.UTF_8);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelSync/1.0 (academic-portfolio; contact@travelsync.edu)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Fetching real destination encyclopedic summary from Wikipedia for '{}'", destination.getName());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());

                String extract = root.path("extract").asText("");
                String description = root.path("description").asText("");
                String imageUrl = null;

                if (root.has("originalimage") && root.path("originalimage").has("source")) {
                    imageUrl = root.path("originalimage").path("source").asText();
                } else if (root.has("thumbnail") && root.path("thumbnail").has("source")) {
                    imageUrl = root.path("thumbnail").path("source").asText();
                }

                destination.setExtract(extract);
                destination.setDescription(!description.isBlank() ? description : extract);
                if (imageUrl != null) {
                    destination.setImageUrl(imageUrl);
                }

                // Detect travel categories based on real encyclopedic content
                inferCategories(destination, extract);
            }
        } catch (Exception ex) {
            log.warn("Wikipedia summary lookup for '{}' was not found or timed out: {}", destination.getName(), ex.getMessage());
            // Fallback description from destination metadata
            if (destination.getDescription() == null || destination.getDescription().isBlank()) {
                destination.setDescription(String.format("%s is a destination in %s, located at coordinates (%.4f, %.4f).",
                        destination.getName(), destination.getCountry(), destination.getLatitude(), destination.getLongitude()));
            }
        }

        // Fallback placeholder image if none returned from API
        if (destination.getImageUrl() == null || destination.getImageUrl().isBlank()) {
            destination.setImageUrl(getDefaultImageForDestination(destination.getName()));
        }

        return destination;
    }

    private void inferCategories(Destination dest, String text) {
        if (text == null) return;
        String lower = text.toLowerCase();
        List<String> cats = new ArrayList<>();
        if (lower.contains("beach") || lower.contains("coast") || lower.contains("sea") || lower.contains("ocean")) cats.add("beach");
        if (lower.contains("temple") || lower.contains("historic") || lower.contains("ancient") || lower.contains("heritage") || lower.contains("monument")) cats.add("historical");
        if (lower.contains("mountain") || lower.contains("trek") || lower.contains("hiking") || lower.contains("adventure") || lower.contains("ski")) cats.add("adventure");
        if (lower.contains("park") || lower.contains("forest") || lower.contains("nature") || lower.contains("wildlife") || lower.contains("valley")) cats.add("nature");
        if (lower.contains("cuisine") || lower.contains("food") || lower.contains("restaurant") || lower.contains("market") || lower.contains("culinary")) cats.add("food");
        if (lower.contains("nightlife") || lower.contains("bar") || lower.contains("club") || lower.contains("entertainment")) cats.add("nightlife");
        if (lower.contains("spa") || lower.contains("resort") || lower.contains("peaceful") || lower.contains("scenic")) cats.add("relaxation");
        if (lower.contains("shopping") || lower.contains("bazaar") || lower.contains("mall")) cats.add("shopping");

        if (cats.isEmpty()) {
            cats.add("historical");
            cats.add("nature");
        }
        dest.setCategories(cats);
    }

    private String getDefaultImageForDestination(String name) {
        return "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1200&q=80";
    }
}
