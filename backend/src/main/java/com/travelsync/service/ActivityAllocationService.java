package com.travelsync.service;

import com.travelsync.dto.ActivityAllocationRequest;
import com.travelsync.engine.MaxFlow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ActivityAllocationService {

    private static final int MAX_TRAVELERS = 100;
    private static final int MAX_ACTIVITIES = 100;

    public record Assignment(
            String travelerId,
            String travelerName,
            String activityId,
            String activityName,
            List<String> matchingInterests
    ) {}

    public record AllocationResult(
            int assignedTravelers,
            int totalTravelers,
            int totalActivities,
            List<Assignment> assignments,
            List<ActivityAllocationRequest.Traveler> unassignedTravelers,
            int graphVertices,
            int graphEdges,
            int augmentations,
            String algorithm,
            String complexity
    ) {}

    public AllocationResult allocate(ActivityAllocationRequest request) {
        if (request == null || request.travelers() == null || request.travelers().isEmpty()) {
            throw new IllegalArgumentException("At least one traveler is required.");
        }
        if (request.activities() == null || request.activities().isEmpty()) {
            throw new IllegalArgumentException("At least one activity is required.");
        }
        if (request.travelers().size() > MAX_TRAVELERS || request.activities().size() > MAX_ACTIVITIES) {
            throw new IllegalArgumentException("The allocation supports at most 100 travelers and 100 activities.");
        }

        validateTravelers(request.travelers());
        validateActivities(request.activities());

        int travelerCount = request.travelers().size();
        int activityCount = request.activities().size();
        int source = 0;
        int travelerStart = 1;
        int activityStart = travelerStart + travelerCount;
        int sink = activityStart + activityCount;
        int vertexCount = sink + 1;

        List<MaxFlow.Edge> edges = new ArrayList<>();
        int[][] assignmentEdgeIndexes = new int[travelerCount][activityCount];
        for (int[] row : assignmentEdgeIndexes) {
            java.util.Arrays.fill(row, -1);
        }

        for (int travelerIndex = 0; travelerIndex < travelerCount; travelerIndex++) {
            int travelerVertex = travelerStart + travelerIndex;
            edges.add(new MaxFlow.Edge(source, travelerVertex, 1));
            Set<String> interests = normalized(request.travelers().get(travelerIndex).interests());

            for (int activityIndex = 0; activityIndex < activityCount; activityIndex++) {
                Set<String> categories = normalized(request.activities().get(activityIndex).categories());
                if (interests.stream().anyMatch(categories::contains)) {
                    assignmentEdgeIndexes[travelerIndex][activityIndex] = edges.size();
                    edges.add(new MaxFlow.Edge(travelerVertex, activityStart + activityIndex, 1));
                }
            }
        }

        for (int activityIndex = 0; activityIndex < activityCount; activityIndex++) {
            edges.add(new MaxFlow.Edge(
                    activityStart + activityIndex,
                    sink,
                    request.activities().get(activityIndex).capacity()
            ));
        }

        MaxFlow.Result flow = MaxFlow.solve(vertexCount, edges, source, sink);
        boolean[] assigned = new boolean[travelerCount];
        List<Assignment> assignments = new ArrayList<>(flow.maxFlow());
        List<Integer> edgeFlows = flow.edgeFlows();

        for (int travelerIndex = 0; travelerIndex < travelerCount; travelerIndex++) {
            for (int activityIndex = 0; activityIndex < activityCount; activityIndex++) {
                int edgeIndex = assignmentEdgeIndexes[travelerIndex][activityIndex];
                if (edgeIndex >= 0 && edgeFlows.get(edgeIndex) > 0) {
                    ActivityAllocationRequest.Traveler traveler = request.travelers().get(travelerIndex);
                    ActivityAllocationRequest.Activity activity = request.activities().get(activityIndex);
                    List<String> matchingInterests = traveler.interests().stream()
                            .filter(interest -> normalized(activity.categories()).contains(normalize(interest)))
                            .distinct()
                            .toList();
                    assignments.add(new Assignment(
                            traveler.id(),
                            traveler.name(),
                            activity.id(),
                            activity.name(),
                            matchingInterests
                    ));
                    assigned[travelerIndex] = true;
                    break;
                }
            }
        }

        List<ActivityAllocationRequest.Traveler> unassigned = new ArrayList<>();
        for (int travelerIndex = 0; travelerIndex < travelerCount; travelerIndex++) {
            if (!assigned[travelerIndex]) {
                unassigned.add(request.travelers().get(travelerIndex));
            }
        }

        return new AllocationResult(
                flow.maxFlow(),
                travelerCount,
                activityCount,
                List.copyOf(assignments),
                List.copyOf(unassigned),
                flow.vertexCount(),
                flow.edgeCount(),
                flow.augmentations(),
                "Edmonds–Karp maximum flow",
                "O(V · E²)"
        );
    }

    private void validateTravelers(List<ActivityAllocationRequest.Traveler> travelers) {
        Set<String> ids = new HashSet<>();
        for (ActivityAllocationRequest.Traveler traveler : travelers) {
            if (traveler == null || isBlank(traveler.id()) || isBlank(traveler.name())) {
                throw new IllegalArgumentException("Every traveler needs a non-empty ID and name.");
            }
            if (!ids.add(traveler.id().trim())) {
                throw new IllegalArgumentException("Traveler IDs must be unique.");
            }
            validateCategories(traveler.interests(), "Each traveler must have at least one interest.");
        }
    }

    private void validateActivities(List<ActivityAllocationRequest.Activity> activities) {
        Set<String> ids = new HashSet<>();
        for (ActivityAllocationRequest.Activity activity : activities) {
            if (activity == null || isBlank(activity.id()) || isBlank(activity.name())) {
                throw new IllegalArgumentException("Every activity needs a non-empty ID and name.");
            }
            if (!ids.add(activity.id().trim())) {
                throw new IllegalArgumentException("Activity IDs must be unique.");
            }
            validateCategories(activity.categories(), "Each activity must have at least one category.");
            if (activity.capacity() <= 0 || activity.capacity() > MAX_TRAVELERS) {
                throw new IllegalArgumentException("Activity capacity must be between 1 and 100.");
            }
        }
    }

    private void validateCategories(List<String> categories, String emptyMessage) {
        if (categories == null || categories.isEmpty() || categories.stream().anyMatch(this::isBlank)) {
            throw new IllegalArgumentException(emptyMessage);
        }
    }

    private Set<String> normalized(List<String> categories) {
        return categories.stream().map(this::normalize).collect(java.util.stream.Collectors.toSet());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
