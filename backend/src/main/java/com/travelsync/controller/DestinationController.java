package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import com.travelsync.model.Destination;
import com.travelsync.model.Place;
import com.travelsync.service.DestinationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Destination>>> search(@RequestParam("query") String query) {
        List<Destination> results = destinationService.searchDestinations(query);
        String msg = results.isEmpty() ? "No destination found." : "Found " + results.size() + " matching destinations.";
        return ResponseEntity.ok(ApiResponse.ok(msg, results).withMetadata("query", query));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<Destination>> getDestination(@PathVariable("name") String name) {
        Destination destination = destinationService.getDestinationByName(name);
        return ResponseEntity.ok(ApiResponse.ok("Destination retrieved successfully", destination));
    }

    @GetMapping("/{name}/places")
    public ResponseEntity<ApiResponse<List<Place>>> getPlaces(@PathVariable("name") String name) {
        List<Place> places = destinationService.getPlacesForDestination(name);
        return ResponseEntity.ok(ApiResponse.ok("Live attractions fetched", places).withMetadata("count", places.size()));
    }

    @GetMapping("/nearby-places")
    public ResponseEntity<ApiResponse<List<Place>>> getNearbyPlaces(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude) {
        List<Place> places = destinationService.getPlacesNear(latitude, longitude);
        return ResponseEntity.ok(ApiResponse.ok("Nearby attractions and hotels fetched", places)
                .withMetadata("count", places.size()));
    }
}
