package com.travelsync.service;

import com.travelsync.engine.SOSDP;
import com.travelsync.model.Destination;
import com.travelsync.model.TravelPreference;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private final DestinationService destinationService;

    public RecommendationService(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    public record RecommendationItem(
            Destination destination,
            int preferenceMask,
            List<String> matchingFeatures,
            double matchScore,
            String matchPercentage
    ) {}

    public record RecommendationResponseData(
            int userPreferenceMask,
            List<String> userPreferences,
            int totalSupersetMatches,
            int totalSubsetMatches,
            List<RecommendationItem> recommendations,
            String algorithm,
            String complexity
    ) {}

    // Diverse reference destinations with authentic travel profiles for instant recommendations
    private static final List<String> POPULAR_SEEDS = List.of(
            "Goa", "Jaipur", "Manali", "Mumbai", "Tokyo", "Paris", "Kyoto", "Rome"
    );

    public RecommendationResponseData getRecommendations(List<String> preferences) {
        int userMask = TravelPreference.toMask(preferences);

        // Fetch destinations for recommendation candidates
        List<Destination> pool = new ArrayList<>();
        for (String seed : POPULAR_SEEDS) {
            try {
                Destination d = destinationService.getDestinationByName(seed);
                pool.add(d);
            } catch (Exception ignored) {}
        }

        // 1. Build mask frequency table for SOS DP
        int totalBits = TravelPreference.TOTAL_BITS; // 8 bits (256 states)
        int[] counts = new int[1 << totalBits];
        Map<Destination, Integer> destMaskMap = new HashMap<>();

        for (Destination d : pool) {
            int mask = TravelPreference.toMask(recommendationCategories(d));
            destMaskMap.put(d, mask);
            counts[mask]++;
        }

        // 2. Run SOS DP
        SOSDP.SosResult sos = SOSDP.compute(counts, totalBits);

        int totalSupersets = sos.supersetSums()[userMask];
        int totalSubsets = sos.subsetSums()[userMask];

        // 3. Rank destinations using SOS DP matching score
        List<RecommendationItem> items = new ArrayList<>();
        for (Destination d : pool) {
            int dMask = destMaskMap.get(d);
            double score = SOSDP.calculateMatchScore(userMask, dMask);
            List<String> matching = TravelPreference.fromMask(userMask & dMask);

            items.add(new RecommendationItem(
                    d,
                    dMask,
                    matching,
                    score,
                    String.format("%.0f%%", score * 100.0)
            ));
        }

        items.sort((a, b) -> Double.compare(b.matchScore(), a.matchScore()));

        return new RecommendationResponseData(
                userMask,
                TravelPreference.fromMask(userMask),
                totalSupersets,
                totalSubsets,
                items,
                "SOS DP (Sum Over Subsets Dynamic Programming)",
                sos.complexity()
        );
    }

    private List<String> recommendationCategories(Destination destination) {
        return switch (destination.getName().toLowerCase(Locale.ROOT)) {
            case "goa" -> List.of("beach", "nightlife", "food", "relaxation");
            case "jaipur" -> List.of("historical", "shopping", "food");
            case "manali" -> List.of("adventure", "nature", "relaxation");
            case "mumbai" -> List.of("beach", "food", "nightlife", "shopping", "historical");
            case "tokyo" -> List.of("historical", "food", "nightlife", "shopping");
            case "paris" -> List.of("historical", "food", "shopping", "relaxation");
            case "kyoto" -> List.of("historical", "nature", "relaxation");
            case "rome" -> List.of("historical", "food", "shopping");
            default -> destination.getCategories();
        };
    }
}
