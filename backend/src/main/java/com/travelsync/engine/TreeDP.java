package com.travelsync.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tree Dynamic Programming (Hierarchical Travel Plan Optimization)
 *
 * Purpose: Optimizes a hierarchical travel plan (Trip -> Days -> Activities)
 *          under a total budget constraint. An activity cannot be included unless
 *          its parent Day is active (Tree Dependent Knapsack).
 * Time Complexity: O(N * B^2) where N is total tree nodes and B is budget
 * Space Complexity: O(N * B) for the dynamic programming table
 * Usage: Choosing the most rewarding combination of trip days and optional activities
 *        within a monetary or time budget.
 */
public class TreeDP {

    public record TreeNode(
            int id,
            String name,
            String type, // "TRIP", "DAY", "ACTIVITY"
            int cost,
            int value,
            List<TreeNode> children
    ) {
        public TreeNode(int id, String name, String type, int cost, int value) {
            this(id, name, type, cost, value, new ArrayList<>());
        }
    }

    public record TreeOptimizationResult(
            int maxValue,
            int totalCostUsed,
            List<String> selectedItems,
            String complexity
    ) {}

    /**
     * Solves the Tree Dependent Knapsack DP on the hierarchical travel plan.
     */
    public static TreeOptimizationResult optimize(TreeNode root, int budget) {
        if (root == null || budget <= 0) {
            return new TreeOptimizationResult(0, 0, List.of(), "O(1)");
        }

        // Collect all nodes to map them to 0..n-1
        List<TreeNode> allNodes = new ArrayList<>();
        flattenTree(root, allNodes);
        int n = allNodes.size();

        // dp[u][b] = max value from subtree u using budget b, assuming u is selected
        int[][] dp = new int[n][budget + 1];
        boolean[][][] choice = new boolean[n][budget + 1][];

        // Bottom-up post-order traversal
        dfs(root, budget, dp);

        int bestVal = 0;
        int bestBudgetUsed = 0;

        for (int b = 0; b <= budget; b++) {
            if (dp[root.id()][b] > bestVal) {
                bestVal = dp[root.id()][b];
                bestBudgetUsed = b;
            }
        }

        // Traceback selected items
        List<String> selectedNames = new ArrayList<>();
        if (bestVal > 0) {
            reconstruct(root, bestBudgetUsed, dp, selectedNames);
        }

        String complexity = String.format("O(N * B^2) = %d states * %d budget", n, budget);
        return new TreeOptimizationResult(bestVal, bestBudgetUsed, selectedNames, complexity);
    }

    private static void flattenTree(TreeNode node, List<TreeNode> list) {
        list.add(node);
        for (TreeNode child : node.children()) {
            flattenTree(child, list);
        }
    }

    private static void dfs(TreeNode node, int maxBudget, int[][] dp) {
        int u = node.id();
        int cost = node.cost();
        int val = node.value();

        // Initialize: if budget < cost, impossible (-1)
        Arrays.fill(dp[u], -1);

        if (cost <= maxBudget) {
            dp[u][cost] = val;
        }

        // Process children
        for (TreeNode child : node.children()) {
            dfs(child, maxBudget, dp);
            int v = child.id();

            int[] nextDp = Arrays.copyOf(dp[u], maxBudget + 1);

            for (int b1 = cost; b1 <= maxBudget; b1++) {
                if (dp[u][b1] < 0) continue;

                for (int b2 = 0; b1 + b2 <= maxBudget; b2++) {
                    if (dp[v][b2] < 0) continue;
                    int candidate = dp[u][b1] + dp[v][b2];
                    if (candidate > nextDp[b1 + b2]) {
                        nextDp[b1 + b2] = candidate;
                    }
                }
            }
            System.arraycopy(nextDp, 0, dp[u], 0, maxBudget + 1);
        }
    }

    private static void reconstruct(TreeNode node, int currentBudget, int[][] dp, List<String> result) {
        result.add(String.format("%s (%s, cost: $%d, val: %d)", node.name(), node.type(), node.cost(), node.value()));

        int remainingBudget = currentBudget - node.cost();
        if (remainingBudget <= 0 || node.children().isEmpty()) {
            return;
        }

        // Allocate budget across children greedily/matching DP sub-results
        for (TreeNode child : node.children()) {
            int v = child.id();
            int bestAlloc = -1;
            int bestVal = -1;

            for (int b = 0; b <= remainingBudget; b++) {
                if (dp[v][b] > bestVal) {
                    bestVal = dp[v][b];
                    bestAlloc = b;
                }
            }

            if (bestVal > 0 && bestAlloc > 0) {
                reconstruct(child, bestAlloc, dp, result);
                remainingBudget -= bestAlloc;
            }
        }
    }
}
