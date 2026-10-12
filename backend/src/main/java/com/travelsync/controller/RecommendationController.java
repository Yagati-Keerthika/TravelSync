package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import com.travelsync.dto.RecommendationRequest;
import com.travelsync.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationService.RecommendationResponseData>> getRecommendations(
            @RequestBody RecommendationRequest request) {
        RecommendationService.RecommendationResponseData result = recommendationService.getRecommendations(
                request.getPreferences()
        );

        return ResponseEntity.ok(ApiResponse.ok("Recommendations matched via SOS DP", result)
                .withMetadata("algorithm", result.algorithm())
                .withMetadata("complexity", result.complexity())
                .withMetadata("totalBits", 8));
    }
}
