package com.travelsync.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bitmask Dynamic Programming (Held-Karp TSP / Tour Optimization)
 *
 * Purpose: Finds the mathematically optimal ordering of visited destinations to
 *          minimize total road distance or travel duration using a real-world cost matrix.
 * Time Complexity: O(2^N * N^2)
 * Space Complexity: O(2^N * N) for the memoization table and predecessor tracking
 * Usage: Intelligent multi-city trip itinerary route ordering.
 */
public class BitmaskDP {

    public record TourResult(
            double minCost,
            List<Integer> optimalOrder,
            String complexity
    ) {}

    /**
     * Solves optimal open tour starting at city startIndex (default 0) and visiting all N destinations.
     *
     * @param costMatrix Real N x N distance or duration matrix
     * @param startIndex Preferred origin city index
     * @return TourResult with minimum cost and permutation of visited cities
     */
    public static TourResult solveOptimalTour(double[][] costMatrix, int startIndex) {
        int n = costMatrix.length;
        if (n <= 1) {
            return new TourResult(0.0, n == 1 ? List.of(0) : List.of(), "O(1)");
        }

        int totalMasks = 1 << n;
        double[][] dp = new double[totalMasks][n];
        int[][] parent = new int[totalMasks][n];

        for (int i = 0; i < totalMasks; i++) {
            Arrays.fill(dp[i], Double.POSITIVE_INFINITY);
            Arrays.fill(parent[i], -1);
        }

        // Base case: starting at startIndex
        int startMask = 1 << startIndex;
        dp[startMask][startIndex] = 0.0;

        // DP transitions
        for (int mask = 0; mask < totalMasks; mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0 || Double.isInfinite(dp[mask][u])) {
                    continue;
                }

                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) == 0) {
                        int nextMask = mask | (1 << v);
                        double newCost = dp[mask][u] + costMatrix[u][v];
                        if (newCost < dp[nextMask][v]) {
                            dp[nextMask][v] = newCost;
                            parent[nextMask][v] = u;
                        }
                    }
                }
            }
        }

        // Find best end city when all n cities are visited (full mask: (1 << n) - 1)
        int fullMask = (1 << n) - 1;
        int bestEnd = -1;
        double minTotalCost = Double.POSITIVE_INFINITY;

        for (int u = 0; u < n; u++) {
            if (dp[fullMask][u] < minTotalCost) {
                minTotalCost = dp[fullMask][u];
                bestEnd = u;
            }
        }

        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        int currCity = bestEnd;
        int currMask = fullMask;

        while (currCity != -1 && currMask > 0) {
            path.add(currCity);
            int prevCity = parent[currMask][currCity];
            currMask ^= (1 << currCity);
            currCity = prevCity;
        }

        Collections.reverse(path);
        String complexity = String.format("O(2^%d * %d^2) = %d states", n, n, (1 << n) * n);

        return new TourResult(minTotalCost, path, complexity);
    }
}
