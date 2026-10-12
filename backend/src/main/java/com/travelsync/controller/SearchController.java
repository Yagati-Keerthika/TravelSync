package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import com.travelsync.dto.SearchRequest;
import com.travelsync.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/kmp")
    public ResponseEntity<ApiResponse<List<Integer>>> kmpSearch(
            @RequestParam("text") String text,
            @RequestParam("pattern") String pattern) {
        long start = System.nanoTime();
        List<Integer> matches = searchService.runKmpSearch(text, pattern);
        long nanos = System.nanoTime() - start;

        return ResponseEntity.ok(ApiResponse.ok("KMP search executed successfully", matches)
                .withMetadata("algorithm", "Knuth-Morris-Pratt (KMP)")
                .withMetadata("timeNanos", nanos)
                .withMetadata("complexity", "O(N + M)")
                .withMetadata("matchCount", matches.size()));
    }

    @GetMapping("/z")
    public ResponseEntity<ApiResponse<List<Integer>>> zSearch(
            @RequestParam("text") String text,
            @RequestParam("pattern") String pattern) {
        long start = System.nanoTime();
        List<Integer> matches = searchService.runZSearch(text, pattern);
        long nanos = System.nanoTime() - start;

        return ResponseEntity.ok(ApiResponse.ok("Z-Function search executed successfully", matches)
                .withMetadata("algorithm", "Z-Function")
                .withMetadata("timeNanos", nanos)
                .withMetadata("complexity", "O(N + M)")
                .withMetadata("matchCount", matches.size()));
    }

    @GetMapping("/naive")
    public ResponseEntity<ApiResponse<List<Integer>>> naiveSearch(
            @RequestParam("text") String text,
            @RequestParam("pattern") String pattern) {
        long start = System.nanoTime();
        List<Integer> matches = searchService.runNaiveSearch(text, pattern);
        long nanos = System.nanoTime() - start;

        return ResponseEntity.ok(ApiResponse.ok("Naive search executed successfully", matches)
                .withMetadata("algorithm", "Naive String Matching")
                .withMetadata("timeNanos", nanos)
                .withMetadata("complexity", "O(N * M)")
                .withMetadata("matchCount", matches.size()));
    }

    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<Map<String, List<Integer>>>> multipleKeywords(
            @RequestBody SearchRequest request) {
        long start = System.nanoTime();
        Map<String, List<Integer>> results = searchService.runMultiKeywordSearch(
                request.getText(), request.getKeywords()
        );
        long nanos = System.nanoTime() - start;

        return ResponseEntity.ok(ApiResponse.ok("Aho-Corasick multi-keyword search complete", results)
                .withMetadata("algorithm", "Aho-Corasick Automaton")
                .withMetadata("timeNanos", nanos)
                .withMetadata("complexity", "O(sum(|P_i|) + |Text| + Occurrences)"));
    }

    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<SearchService.TypoSuggestion>>> suggest(
            @RequestParam("query") String query) {
        List<SearchService.TypoSuggestion> suggestions = searchService.getTypoSuggestions(query);
        String msg = suggestions.isEmpty() ? "No typo corrections found" : "Found suggested destinations";

        return ResponseEntity.ok(ApiResponse.ok(msg, suggestions)
                .withMetadata("algorithm", "Edit Distance (Wagner-Fischer)")
                .withMetadata("complexity", "O(N * M)")
                .withMetadata("query", query));
    }
}
