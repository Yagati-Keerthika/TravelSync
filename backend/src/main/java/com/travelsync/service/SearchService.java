package com.travelsync.service;

import com.travelsync.engine.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {

    // Common reference global destinations for typo candidate matching
    private static final List<String> REFERENCE_DESTINATIONS = List.of(
            "Hyderabad", "Mumbai", "Delhi", "Bengaluru", "Chennai", "Kolkata", "Jaipur", "Manali",
            "Goa", "Shimla", "Vijayawada", "Agra", "Varanasi", "Kochi", "Amritsar", "Udaipur",
            "Tokyo", "Kyoto", "Osaka", "Paris", "London", "Rome", "New York", "San Francisco",
            "Singapore", "Bangkok", "Dubai", "Sydney", "Barcelona", "Amsterdam", "Berlin", "Cairo"
    );

    public record TypoSuggestion(
            String query,
            String suggestedTarget,
            int editDistance,
            double similarity,
            List<EditDistance.EditOp> operations
    ) {}

    public List<Integer> runNaiveSearch(String text, String pattern) {
        return NaiveSearch.search(text, pattern);
    }

    public List<Integer> runKmpSearch(String text, String pattern) {
        return KMP.search(text, pattern);
    }

    public List<Integer> runZSearch(String text, String pattern) {
        return ZFunction.search(text, pattern);
    }

    public Map<String, List<Integer>> runMultiKeywordSearch(String text, Collection<String> keywords) {
        return AhoCorasick.searchAll(text, keywords);
    }

    public List<TypoSuggestion> getTypoSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String normalized = query.trim().toLowerCase();
        List<TypoSuggestion> suggestions = new ArrayList<>();

        for (String target : REFERENCE_DESTINATIONS) {
            EditDistance.Result res = EditDistance.compute(normalized, target.toLowerCase());
            // If very close (distance <= 3 and similarity >= 0.6)
            if (res.distance() <= 3 && res.similarity() >= 0.55) {
                suggestions.add(new TypoSuggestion(
                        query,
                        target,
                        res.distance(),
                        Math.round(res.similarity() * 100.0) / 100.0,
                        res.operations()
                ));
            }
        }

        suggestions.sort(Comparator.comparingInt(TypoSuggestion::editDistance)
                .thenComparing((a, b) -> Double.compare(b.similarity(), a.similarity())));

        return suggestions;
    }
}
