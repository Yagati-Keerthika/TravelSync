package com.travelsync.controller;

import com.travelsync.client.RoutingApiClient;
import com.travelsync.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RoutingApiClient routingClient;

    public RouteController(RoutingApiClient routingClient) {
        this.routingClient = routingClient;
    }

    @GetMapping("/directions")
    public ResponseEntity<ApiResponse<RoutingApiClient.DrivingDirections>> getDrivingDirections(
            @RequestParam double originLatitude,
            @RequestParam double originLongitude,
            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude
    ) {
        validateCoordinates(originLatitude, originLongitude, "origin");
        validateCoordinates(destinationLatitude, destinationLongitude, "destination");

        RoutingApiClient.DrivingDirections directions = routingClient.getDrivingDirections(
                originLatitude, originLongitude, destinationLatitude, destinationLongitude);
        return ResponseEntity.ok(ApiResponse.ok("Driving directions retrieved", directions)
                .withMetadata("provider", "OSRM"));
    }

    private void validateCoordinates(double latitude, double longitude, String point) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid coordinates for route " + point + ".");
        }
    }
}
