package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import com.travelsync.dto.CompareRequest;
import com.travelsync.service.ComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compare")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping("/destinations")
    public ResponseEntity<ApiResponse<ComparisonService.DestinationComparisonResult>> compareDestinations(
            @RequestBody CompareRequest request) {
        if (request.getDestination1() == null || request.getDestination2() == null) {
            throw new IllegalArgumentException("Two destination names are required for comparison.");
        }

        ComparisonService.DestinationComparisonResult result = comparisonService.compareDestinations(
                request.getDestination1(), request.getDestination2()
        );

        return ResponseEntity.ok(ApiResponse.ok("Destination comparison completed using Suffix Array + LCP", result)
                .withMetadata("algorithms", "Suffix Array + Kasai LCP")
                .withMetadata("lcpComplexity", "O(N) Kasai"));
    }

    @PostMapping("/descriptions")
    public ResponseEntity<ApiResponse<ComparisonService.DescriptionAlignmentResult>> compareDescriptions(
            @RequestBody CompareRequest request) {
        ComparisonService.DescriptionAlignmentResult result = comparisonService.compareDescriptions(
                request.getText1(), request.getText2()
        );

        return ResponseEntity.ok(ApiResponse.ok("Sequence alignment completed", result)
                .withMetadata("globalAlgorithm", "Needleman-Wunsch (Global Alignment)")
                .withMetadata("localAlgorithm", "Smith-Waterman (Local Alignment)")
                .withMetadata("complexity", "O(N * M)"));
    }
}
