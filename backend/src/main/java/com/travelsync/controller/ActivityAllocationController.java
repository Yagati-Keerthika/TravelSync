package com.travelsync.controller;

import com.travelsync.dto.ActivityAllocationRequest;
import com.travelsync.dto.ApiResponse;
import com.travelsync.service.ActivityAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
public class ActivityAllocationController {

    private final ActivityAllocationService allocationService;

    public ActivityAllocationController(ActivityAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<ActivityAllocationService.AllocationResult>> allocateTravelers(
            @RequestBody ActivityAllocationRequest request
    ) {
        ActivityAllocationService.AllocationResult result = allocationService.allocate(request);
        return ResponseEntity.ok(ApiResponse.ok("Travelers allocated to activities via maximum flow", result)
                .withMetadata("algorithm", result.algorithm())
                .withMetadata("complexity", result.complexity())
                .withMetadata("graphVertices", result.graphVertices())
                .withMetadata("graphEdges", result.graphEdges())
                .withMetadata("augmentations", result.augmentations()));
    }
}
