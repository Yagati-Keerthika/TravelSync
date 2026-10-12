package com.travelsync.client;

import com.travelsync.model.Destination;
import java.util.List;

public interface RoutingApiClient {
    RouteMatrix getRouteMatrix(List<Destination> destinations);

    DrivingDirections getDrivingDirections(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    );

    record RouteMatrix(
            double[][] distanceMatrixMeters,
            double[][] durationMatrixSeconds,
            String provider
    ) {}

    record DrivingDirections(
            double distanceMeters,
            double durationSeconds,
            String formattedDistance,
            String formattedDuration,
            List<RouteStep> steps
    ) {}

    record RouteStep(String instruction, String roadName, double distanceMeters, double durationSeconds) {}
}
