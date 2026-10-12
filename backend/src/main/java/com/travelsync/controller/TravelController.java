package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hello() {
        Map<String, Object> data = Map.of(
                "app", "TravelSync - Intelligent Travel Planning & Information Platform",
                "version", "1.0.0",
                "status", "ONLINE",
                "dsaEngines", List.of(
                        "NaiveSearch", "KMP", "ZFunction", "RabinKarp", "AhoCorasick",
                        "EditDistance", "NeedlemanWunsch", "SmithWaterman",
                        "SuffixArray", "LCP", "BitmaskDP", "IntervalDP", "TreeDP", "SOSDP"
                ),
                "externalApis", List.of(
                        "Open-Meteo Geocoding",
                        "Wikipedia MediaWiki REST API",
                        "OSRM Routing Engine",
                        "OpenStreetMap Overpass API"
                )
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.ok(
                "TravelSync Backend is active and connected.",
                data
        );
        return ResponseEntity.ok(response);
    }
}
