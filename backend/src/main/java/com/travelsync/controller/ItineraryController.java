package com.travelsync.controller;

import com.travelsync.dto.ActivityScheduleRequest;
import com.travelsync.dto.ApiResponse;
import com.travelsync.dto.ItineraryOptimizeRequest;
import com.travelsync.engine.IntervalDP;
import com.travelsync.engine.TreeDP;
import com.travelsync.model.Itinerary;
import com.travelsync.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/api/itinerary/optimize")
    public ResponseEntity<ApiResponse<Itinerary>> optimizeItinerary(@RequestBody ItineraryOptimizeRequest request) {
        Itinerary itinerary = itineraryService.optimizeTrip(
                request.getDestinations(),
                request.getObjective() != null ? request.getObjective() : "DISTANCE",
                request.getStartIndex()
        );

        return ResponseEntity.ok(ApiResponse.ok("Itinerary optimized successfully via Bitmask DP", itinerary)
                .withMetadata("algorithm", "Bitmask Dynamic Programming (Held-Karp)")
                .withMetadata("complexity", itinerary.getComplexity())
                .withMetadata("executionNanos", itinerary.getExecutionTimeNanos()));
    }

    @PostMapping("/api/activities/optimize")
    public ResponseEntity<ApiResponse<IntervalDP.ScheduleResult>> optimizeActivities(
            @RequestBody ActivityScheduleRequest request) {
        IntervalDP.ScheduleResult result = itineraryService.optimizeActivities(request.getActivities());

        return ResponseEntity.ok(ApiResponse.ok("Activities scheduled via Interval DP", result)
                .withMetadata("algorithm", "Interval Dynamic Programming (Weighted Interval Scheduling)")
                .withMetadata("complexity", result.complexity()));
    }

    public record TreePlanRequest(TreeDP.TreeNode plan, int budget) {}

    @PostMapping("/api/travel-plan/optimize")
    public ResponseEntity<ApiResponse<TreeDP.TreeOptimizationResult>> optimizeTravelPlan(
            @RequestBody TreePlanRequest request) {
        TreeDP.TreeOptimizationResult result = itineraryService.optimizeTravelPlan(
                request.plan(), request.budget()
        );

        return ResponseEntity.ok(ApiResponse.ok("Hierarchical plan optimized via Tree DP", result)
                .withMetadata("algorithm", "Tree Dynamic Programming (Dependent Knapsack on Trees)")
                .withMetadata("complexity", result.complexity()));
    }
}
