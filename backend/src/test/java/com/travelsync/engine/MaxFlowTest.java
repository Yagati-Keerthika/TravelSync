package com.travelsync.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaxFlowTest {

    @Test
    void findsMaximumFlowAndReportsOriginalEdgeFlows() {
        List<MaxFlow.Edge> edges = List.of(
                new MaxFlow.Edge(0, 1, 3),
                new MaxFlow.Edge(0, 2, 2),
                new MaxFlow.Edge(1, 2, 1),
                new MaxFlow.Edge(1, 3, 2),
                new MaxFlow.Edge(2, 3, 3)
        );

        MaxFlow.Result result = MaxFlow.solve(4, edges, 0, 3);

        assertEquals(5, result.maxFlow());
        assertEquals(3, result.augmentations());
        assertEquals(List.of(3, 2, 1, 2, 3), result.edgeFlows());
    }

    @Test
    void reroutesFlowThroughReverseResidualEdges() {
        List<MaxFlow.Edge> edges = List.of(
                new MaxFlow.Edge(0, 1, 1),
                new MaxFlow.Edge(0, 2, 1),
                new MaxFlow.Edge(1, 2, 1),
                new MaxFlow.Edge(1, 3, 1),
                new MaxFlow.Edge(2, 3, 1)
        );

        MaxFlow.Result result = MaxFlow.solve(4, edges, 0, 3);

        assertEquals(2, result.maxFlow());
        assertEquals(5, result.edgeFlows().size());
        assertEquals(0, result.edgeFlows().get(2));
    }

    @Test
    void supportsParallelEdges() {
        List<MaxFlow.Edge> edges = List.of(
                new MaxFlow.Edge(0, 1, 1),
                new MaxFlow.Edge(0, 1, 1),
                new MaxFlow.Edge(1, 2, 2)
        );

        MaxFlow.Result result = MaxFlow.solve(3, edges, 0, 2);

        assertEquals(2, result.maxFlow());
        assertEquals(2, result.edgeFlows().get(0) + result.edgeFlows().get(1));
    }

    @Test
    void rejectsInvalidNetworkDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> MaxFlow.solve(1, List.of(), 0, 0));
        assertThrows(IllegalArgumentException.class, () -> MaxFlow.solve(2, List.of(), 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> MaxFlow.solve(2, List.of(new MaxFlow.Edge(0, 2, 1)), 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> MaxFlow.solve(2, List.of(new MaxFlow.Edge(0, 1, -1)), 0, 1));
    }
}
