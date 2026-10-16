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

                boolean disambiguation = root.path("type").asText("").equalsIgnoreCase("disambiguation")
                        || extract.toLowerCase(java.util.Locale.ROOT).contains("may refer to:");
                if (disambiguation) {
                    extract = "";
                    description = "";
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
        }

        if (destination.getDescription() == null || destination.getDescription().isBlank()) {
            String profile = fallbackProfile(destination.getName());
            String location = destination.getRegion() == null || destination.getRegion().isBlank()
                    ? destination.getCountry() : destination.getRegion() + ", " + destination.getCountry();
            destination.setDescription(profile.isBlank()
                    ? String.format("%s is a city in %s. Explore its nearby landmarks, local places and route options using TravelSync.",
                    destination.getName(), location)
                    : profile);
            destination.setExtract(destination.getDescription());
            inferCategories(destination, destination.getDescription());
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

    private String fallbackProfile(String name) {
        return switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "manali" -> "Manali is a Himalayan resort town in Himachal Pradesh, known for mountain scenery, the Beas River, trekking and winter sports. It is a popular base for exploring the Kullu Valley.";
            case "goa" -> "Goa is a coastal state in western India known for Arabian Sea beaches, Portuguese-influenced heritage, seafood, markets and a lively nightlife.";
            case "shimla" -> "Shimla is a hill city in Himachal Pradesh, known for its mountain views, colonial-era architecture, The Ridge and nearby Himalayan trails.";
            case "jaipur" -> "Jaipur, Rajasthan's capital, is known for its historic forts and palaces, pink-hued old city, craft markets and regional cuisine.";
            case "mumbai" -> "Mumbai is a coastal metropolis in Maharashtra known for its seafront, historic architecture, performing arts, markets and diverse food.";
            case "bengaluru", "bangalore" -> "Bengaluru is Karnataka's capital, known for its gardens, technology district, cafés, museums and lively food scene.";
            case "tokyo" -> "Tokyo is Japan's capital, combining historic temples and gardens with modern neighborhoods, museums, shopping and distinctive food.";
            case "paris" -> "Paris is France's capital, known for its museums, historic architecture, gardens, cafés and landmarks along the Seine.";
            case "kyoto" -> "Kyoto is known for its historic temples, traditional gardens, shrines and preserved neighborhoods in central Japan.";
            case "rome" -> "Rome is Italy's capital, known for ancient ruins, historic churches, art, piazzas and celebrated local cuisine.";
            default -> "";
        };
    }
}
