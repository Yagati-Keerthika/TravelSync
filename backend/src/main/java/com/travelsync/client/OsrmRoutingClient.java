package com.travelsync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsync.exception.ExternalApiException;
import com.travelsync.model.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;

@Component
public class OsrmRoutingClient implements RoutingApiClient {

    private static final Logger log = LoggerFactory.getLogger(OsrmRoutingClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${travelsync.api.osrm.table-url:https://router.project-osrm.org/table/v1/driving}")
    private String tableUrl;

    @Value("${travelsync.api.osrm.route-url:https://router.project-osrm.org/route/v1/driving}")
    private String routeUrl;

    public OsrmRoutingClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RouteMatrix getRouteMatrix(List<Destination> destinations) {
        int n = destinations.size();
        if (n <= 1) {
            return new RouteMatrix(new double[n][n], new double[n][n], "Identity (Single Point)");
        }

        // Construct OSRM coordinate list: lon1,lat1;lon2,lat2;...
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < n; i++) {
            Destination d = destinations.get(i);
            if (i > 0) coords.append(";");
            coords.append(String.format(Locale.US, "%.5f,%.5f", d.getLongitude(), d.getLatitude()));
        }

        String url = String.format("%s/%s?annotations=distance,duration", tableUrl, coords);

        try {
            log.info("Requesting live real-world road distance/duration matrix from OSRM: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null && !response.isBlank()) {
                JsonNode root = objectMapper.readTree(response);
                if ("Ok".equalsIgnoreCase(root.path("code").asText())) {
                    JsonNode distancesNode = root.path("distances");
                    JsonNode durationsNode = root.path("durations");

                    double[][] distMatrix = new double[n][n];
                    double[][] durMatrix = new double[n][n];

                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            distMatrix[i][j] = distancesNode.get(i).get(j).asDouble(0.0);
                            durMatrix[i][j] = durationsNode.get(i).get(j).asDouble(0.0);
                        }
                    }
                    log.info("Successfully fetched {}x{} road distance matrix from OSRM", n, n);
                    return new RouteMatrix(distMatrix, durMatrix, "OSRM Live Road Network");
                }
            }
        } catch (Exception ex) {
            log.warn("OSRM routing API call failed or timed out: {}. Calculating Great-Circle (Haversine) matrix fallback.", ex.getMessage());
        }

        // Geodesic fallback if network times out
        return computeHaversineMatrix(destinations);
    }

    @Override
    public DrivingDirections getDrivingDirections(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    ) {
        String coordinates = String.format(Locale.US, "%.5f,%.5f;%.5f,%.5f",
                originLongitude, originLatitude, destinationLongitude, destinationLatitude);
        String url = String.format("%s/%s?overview=false&steps=true", routeUrl, coordinates);

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                throw new ExternalApiException("OSRM", "The routing provider returned an empty response.");
            }

            JsonNode root = objectMapper.readTree(response);
            if (!"Ok".equalsIgnoreCase(root.path("code").asText())) {
                throw new ExternalApiException("OSRM",
                        root.path("message").asText("No driving route is available for these locations."));
            }

            JsonNode route = root.path("routes").path(0);
            if (route.isMissingNode()) {
                throw new ExternalApiException("OSRM", "No driving route is available for these locations.");
            }

            List<RouteStep> steps = new java.util.ArrayList<>();
            JsonNode legSteps = route.path("legs").path(0).path("steps");
            for (JsonNode step : legSteps) {
                double distanceMeters = step.path("distance").asDouble();
                if (distanceMeters <= 0) {
                    continue;
                }
                String roadName = step.path("name").asText("");
                String instruction = formatInstruction(step.path("maneuver"), roadName);
                steps.add(new RouteStep(instruction, roadName, distanceMeters, step.path("duration").asDouble()));
            }

            double distanceMeters = route.path("distance").asDouble();
            double durationSeconds = route.path("duration").asDouble();
            log.info("Fetched driving directions from OSRM ({} meters, {} steps)",
                    distanceMeters, steps.size());
            return new DrivingDirections(distanceMeters, durationSeconds,
                    formatDistance(distanceMeters), formatDuration(durationSeconds), List.copyOf(steps));
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalApiException("OSRM", "Could not retrieve driving directions.", ex);
        }
    }

    private String formatInstruction(JsonNode maneuver, String roadName) {
        String type = maneuver.path("type").asText("continue");
        String modifier = maneuver.path("modifier").asText("");
        String direction = modifier.isBlank() ? "" : " " + modifier.replace('_', ' ');
        String road = roadName.isBlank() ? "" : " onto " + roadName;

        return switch (type) {
            case "depart" -> "Head" + direction + (road.isBlank() ? " along the route" : road);
            case "arrive" -> "Arrive at your destination";
            case "turn", "end of road", "fork", "off ramp", "on ramp", "exit roundabout", "exit rotary" ->
                    capitalize(type.equals("turn") ? "Turn" + direction : type.replace('_', ' ') + direction) + road;
            case "roundabout", "rotary" -> "At the roundabout" + direction + road;
            case "merge" -> "Merge" + direction + road;
            case "new name", "continue" -> (modifier.isBlank() ? "Continue" : "Continue" + direction) + road;
            default -> capitalize(type.replace('_', ' ')) + direction + road;
        };
    }

    private String formatDistance(double distanceMeters) {
        return distanceMeters >= 1000
                ? String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
                : String.format(Locale.US, "%.0f m", distanceMeters);
    }

    private String formatDuration(double durationSeconds) {
        long minutes = Math.round(durationSeconds / 60.0);
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours > 0
                ? String.format(Locale.US, "%d hr %d min", hours, remainingMinutes)
                : String.format(Locale.US, "%d min", minutes);
    }

    private String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private RouteMatrix computeHaversineMatrix(List<Destination> destinations) {
        int n = destinations.size();
        double[][] dist = new double[n][n];
        double[][] dur = new double[n][n];
        final double R = 6371000.0; // Earth radius in meters
        final double AVG_SPEED_MPS = 18.0; // ~65 km/h

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    dur[i][j] = 0;
                } else {
                    double lat1 = Math.toRadians(destinations.get(i).getLatitude());
                    double lon1 = Math.toRadians(destinations.get(i).getLongitude());
                    double lat2 = Math.toRadians(destinations.get(j).getLatitude());
                    double lon2 = Math.toRadians(destinations.get(j).getLongitude());

                    double dlat = lat2 - lat1;
                    double dlon = lon2 - lon1;

                    double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    double d = R * c * 1.25; // 1.25 winding factor for roads

                    dist[i][j] = d;
                    dur[i][j] = d / AVG_SPEED_MPS;
                }
            }
        }
        return new RouteMatrix(dist, dur, "Haversine Geodesic (Road Approximation)");
    }
}
