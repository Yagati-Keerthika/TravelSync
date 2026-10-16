package com.travelsync.service;

import com.travelsync.dto.ActivityAllocationRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityAllocationServiceTest {

    private final ActivityAllocationService service = new ActivityAllocationService();

    @Test
    void allocatesTravelersWithoutExceedingCapacityOrInterests() {
        ActivityAllocationRequest request = new ActivityAllocationRequest(
                List.of(
                        new ActivityAllocationRequest.Traveler("t1", "Asha", List.of("nature")),
                        new ActivityAllocationRequest.Traveler("t2", "Ben", List.of("culture")),
                        new ActivityAllocationRequest.Traveler("t3", "Cara", List.of("nature", "culture"))
                ),
                List.of(
                        new ActivityAllocationRequest.Activity("a1", "Nature walk", List.of("nature"), 1),
                        new ActivityAllocationRequest.Activity("a2", "Museum", List.of("culture"), 2)
                )
        );

        ActivityAllocationService.AllocationResult result = service.allocate(request);

        assertEquals(3, result.assignedTravelers());
        assertEquals(3, result.assignments().size());
        assertEquals(0, result.unassignedTravelers().size());
        assertEquals(1, result.assignments().stream()
                .filter(assignment -> assignment.activityId().equals("a1")).count());
        assertEquals(2, result.assignments().stream()
                .filter(assignment -> assignment.activityId().equals("a2")).count());
        assertEquals("Edmonds–Karp maximum flow", result.algorithm());
    }

    @Test
    void maximizesAllocationsInsteadOfGreedilyTakingFirstSuitableActivity() {
        ActivityAllocationRequest request = new ActivityAllocationRequest(
                List.of(
                        new ActivityAllocationRequest.Traveler("t1", "Flexible", List.of("nature", "culture")),
                        new ActivityAllocationRequest.Traveler("t2", "Nature only", List.of("nature"))
                ),
                List.of(
                        new ActivityAllocationRequest.Activity("a1", "Nature walk", List.of("nature"), 1),
                        new ActivityAllocationRequest.Activity("a2", "Museum", List.of("culture"), 1)
                )
        );

        ActivityAllocationService.AllocationResult result = service.allocate(request);

        assertEquals(2, result.assignedTravelers());
        assertEquals("a2", result.assignments().stream()
                .filter(assignment -> assignment.travelerId().equals("t1"))
                .findFirst().orElseThrow().activityId());
        assertEquals("a1", result.assignments().stream()
                .filter(assignment -> assignment.travelerId().equals("t2"))
                .findFirst().orElseThrow().activityId());
    }

    @Test
    void reportsTravelersWhoHaveNoCompatibleAvailableCapacity() {
        ActivityAllocationRequest request = new ActivityAllocationRequest(
                List.of(
                        new ActivityAllocationRequest.Traveler("t1", "Asha", List.of("nature")),
                        new ActivityAllocationRequest.Traveler("t2", "Ben", List.of("food"))
                ),
                List.of(new ActivityAllocationRequest.Activity("a1", "Museum", List.of("culture"), 1))
        );

        ActivityAllocationService.AllocationResult result = service.allocate(request);

        assertEquals(0, result.assignedTravelers());
        assertEquals(List.of("Asha", "Ben"),
                result.unassignedTravelers().stream().map(ActivityAllocationRequest.Traveler::name).toList());
    }

    @Test
    void rejectsDuplicateIdsAndInvalidCapacity() {
        ActivityAllocationRequest duplicateIds = new ActivityAllocationRequest(
                List.of(
                        new ActivityAllocationRequest.Traveler("t1", "Asha", List.of("nature")),
                        new ActivityAllocationRequest.Traveler("t1", "Ben", List.of("culture"))
                ),
                List.of(new ActivityAllocationRequest.Activity("a1", "Museum", List.of("culture"), 1))
        );
        ActivityAllocationRequest invalidCapacity = new ActivityAllocationRequest(
                List.of(new ActivityAllocationRequest.Traveler("t1", "Asha", List.of("nature"))),
                List.of(new ActivityAllocationRequest.Activity("a1", "Museum", List.of("culture"), 0))
        );

        assertThrows(IllegalArgumentException.class, () -> service.allocate(duplicateIds));
        assertThrows(IllegalArgumentException.class, () -> service.allocate(invalidCapacity));
    }
}
