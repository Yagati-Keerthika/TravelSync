package com.travelsync.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Edmonds–Karp maximum flow using breadth-first augmenting paths.
 */
public final class MaxFlow {

    public record Edge(int from, int to, int capacity) {}

    public record Result(int maxFlow, int augmentations, int vertexCount, int edgeCount, List<Integer> edgeFlows) {}

    private static final class ResidualEdge {
        private final int to;
        private final int reverseIndex;
        private final int originalCapacity;
        private int residualCapacity;

        private ResidualEdge(int to, int reverseIndex, int capacity) {
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.originalCapacity = capacity;
            this.residualCapacity = capacity;
        }
    }

    private MaxFlow() {}

    public static Result solve(int vertexCount, List<Edge> edges, int source, int sink) {
        if (vertexCount < 2) {
            throw new IllegalArgumentException("A flow network must contain at least two vertices.");
        }
        if (edges == null) {
            throw new IllegalArgumentException("Flow network edges are required.");
        }
        if (source < 0 || source >= vertexCount || sink < 0 || sink >= vertexCount || source == sink) {
            throw new IllegalArgumentException("Source and sink must be distinct vertices in the network.");
        }

        List<List<ResidualEdge>> graph = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            graph.add(new ArrayList<>());
        }

        List<ResidualEdge> originalEdges = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            if (edge == null || edge.from() < 0 || edge.from() >= vertexCount
                    || edge.to() < 0 || edge.to() >= vertexCount || edge.from() == edge.to()
                    || edge.capacity() < 0) {
                throw new IllegalArgumentException("Every flow edge must have valid distinct endpoints and non-negative capacity.");
            }

            List<ResidualEdge> fromEdges = graph.get(edge.from());
            List<ResidualEdge> toEdges = graph.get(edge.to());
            ResidualEdge forward = new ResidualEdge(edge.to(), toEdges.size(), edge.capacity());
            ResidualEdge reverse = new ResidualEdge(edge.from(), fromEdges.size(), 0);
            fromEdges.add(forward);
            toEdges.add(reverse);
            originalEdges.add(forward);
        }

        int maxFlow = 0;
        int augmentations = 0;
        int[] parentVertex = new int[vertexCount];
        int[] parentEdgeIndex = new int[vertexCount];

        while (findAugmentingPath(graph, source, sink, parentVertex, parentEdgeIndex)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int vertex = sink; vertex != source; vertex = parentVertex[vertex]) {
                ResidualEdge edge = graph.get(parentVertex[vertex]).get(parentEdgeIndex[vertex]);
                pathFlow = Math.min(pathFlow, edge.residualCapacity);
            }

            for (int vertex = sink; vertex != source; vertex = parentVertex[vertex]) {
                ResidualEdge edge = graph.get(parentVertex[vertex]).get(parentEdgeIndex[vertex]);
                edge.residualCapacity -= pathFlow;
                graph.get(vertex).get(edge.reverseIndex).residualCapacity += pathFlow;
            }

            maxFlow = Math.addExact(maxFlow, pathFlow);
            augmentations++;
        }

        List<Integer> edgeFlows = originalEdges.stream()
                .map(edge -> edge.originalCapacity - edge.residualCapacity)
                .toList();
        return new Result(maxFlow, augmentations, vertexCount, edges.size(), edgeFlows);
    }

    private static boolean findAugmentingPath(
            List<List<ResidualEdge>> graph,
            int source,
            int sink,
            int[] parentVertex,
            int[] parentEdgeIndex
    ) {
        Arrays.fill(parentVertex, -1);
        parentVertex[source] = source;
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty() && parentVertex[sink] == -1) {
            int current = queue.remove();
            List<ResidualEdge> outgoing = graph.get(current);
            for (int edgeIndex = 0; edgeIndex < outgoing.size(); edgeIndex++) {
                ResidualEdge edge = outgoing.get(edgeIndex);
                if (edge.residualCapacity > 0 && parentVertex[edge.to] == -1) {
                    parentVertex[edge.to] = current;
                    parentEdgeIndex[edge.to] = edgeIndex;
                    queue.add(edge.to);
                    if (edge.to == sink) {
                        break;
                    }
                }
            }
        }

        return parentVertex[sink] != -1;
    }
}
