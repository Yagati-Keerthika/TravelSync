package com.travelsync.service;

import com.travelsync.client.RoutingApiClient;
import com.travelsync.engine.BitmaskDP;
import com.travelsync.engine.IntervalDP;
import com.travelsync.engine.TreeDP;
import com.travelsync.model.Destination;
import com.travelsync.model.Itinerary;
import com.travelsync.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItineraryService {

    private static final Logger log = LoggerFactory.getLogger(ItineraryService.class);

    private final DestinationService destinationService;
    private final RoutingApiClient routingClient;

    public ItineraryService(DestinationService destinationService, RoutingApiClient routingClient) {
        this.destinationService = destinationService;
        this.routingClient = routingClient;
    }

    public Itinerary optimizeTrip(List<String> destinationNames, String objective, int startIndex) {
        if (destinationNames == null || destinationNames.isEmpty()) {
            throw new IllegalArgumentException("At least one destination is required for itinerary optimization.");
        }

        long startNano = System.nanoTime();

        // 1. Resolve real coordinates for every destination
        List<Destination> destinations = new ArrayList<>();
        for (String name : destinationNames) {
            Destination dest = destinationService.getDestinationByName(name.trim());
            destinations.add(dest);
        }

        int n = destinations.size();
        if (n == 1) {
            Itinerary single = new Itinerary();
            single.setOrderedDestinations(destinations);
            single.setTotalDistanceMeters(0);
            single.setTotalDurationSeconds(0);
            single.setAlgorithmUsed("Bitmask DP (Trivial Single Node)");
            single.setExecutionTimeNanos(System.nanoTime() - startNano);
            single.setComplexity("O(1)");
            return single;
        }

        // 2. Fetch real road distance and duration matrix from OSRM Routing API
        RoutingApiClient.RouteMatrix routeMatrix = routingClient.getRouteMatrix(destinations);

        double[][] costMatrix = "DURATION".equalsIgnoreCase(objective)
                ? routeMatrix.durationMatrixSeconds()
                : routeMatrix.distanceMatrixMeters();

        // 3. Optimize sequence via Bitmask Dynamic Programming
        int validStart = (startIndex >= 0 && startIndex < n) ? startIndex : 0;
        BitmaskDP.TourResult tour = BitmaskDP.solveOptimalTour(costMatrix, validStart);

        // 4. Construct the resulting ordered itinerary
        Itinerary itinerary = new Itinerary();
        List<Destination> ordered = new ArrayList<>();
        List<Route> legs = new ArrayList<>();

        double totalDist = 0.0;
        double totalDur = 0.0;

        for (int i = 0; i < tour.optimalOrder().size(); i++) {
            int currentIdx = tour.optimalOrder().get(i);
            ordered.add(destinations.get(currentIdx));

            if (i > 0) {
                int prevIdx = tour.optimalOrder().get(i - 1);
                double legDist = routeMatrix.distanceMatrixMeters()[prevIdx][currentIdx];
                double legDur = routeMatrix.durationMatrixSeconds()[prevIdx][currentIdx];
                totalDist += legDist;
                totalDur += legDur;

                legs.add(new Route(
                        destinations.get(prevIdx).getName(),
                        destinations.get(currentIdx).getName(),
                        legDist,
                        legDur
                ));
            }
        }

        itinerary.setOrderedDestinations(ordered);
        itinerary.setLegs(legs);
        itinerary.setTotalDistanceMeters(totalDist);
        itinerary.setTotalDurationSeconds(totalDur);
        itinerary.setAlgorithmUsed(String.format("Bitmask DP (%s optimization via %s)",
                "DURATION".equalsIgnoreCase(objective) ? "Travel Duration" : "Road Distance",
                routeMatrix.provider()));
        itinerary.setExecutionTimeNanos(System.nanoTime() - startNano);
        itinerary.setComplexity(tour.complexity());

        return itinerary;
    }

    public IntervalDP.ScheduleResult optimizeActivities(List<IntervalDP.Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return new IntervalDP.ScheduleResult(0.0, List.of(), 0, "O(1)");
        }
        return IntervalDP.optimizeSchedule(activities);
    }

    public TreeDP.TreeOptimizationResult optimizeTravelPlan(TreeDP.TreeNode plan, int budget) {
        if (plan == null) {
            return new TreeDP.TreeOptimizationResult(0, 0, List.of(), "O(1)");
        }
        return TreeDP.optimize(plan, budget);
    }
}
