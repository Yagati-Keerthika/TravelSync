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

        int[] rootDp = solveSubtree(root, budget);
        int bestVal = 0;
        int bestBudgetUsed = 0;

        for (int b = 0; b <= budget; b++) {
            if (rootDp[b] > bestVal) {
                bestVal = rootDp[b];
                bestBudgetUsed = b;
            }
        }

        List<String> selectedNames = new ArrayList<>();
        if (bestVal > 0) {
            selectedNames.addAll(reconstructSelection(root, bestBudgetUsed));
        }

        String complexity = String.format("O(N * B^2) = %d states * %d budget", countNodes(root), budget);
        return new TreeOptimizationResult(bestVal, bestBudgetUsed, selectedNames, complexity);
    }

    private static int countNodes(TreeNode node) {
        if (node == null) {
            return 0;
        }
        int total = 1;
        for (TreeNode child : node.children()) {
            total += countNodes(child);
        }
        return total;
    }

    private static int[] solveSubtree(TreeNode node, int budget) {
        int[] childrenDp = new int[budget + 1];
        Arrays.fill(childrenDp, 0);

        for (TreeNode child : node.children()) {
            int[] childDp = solveSubtree(child, budget);
            childrenDp = mergeValues(childrenDp, childDp, budget);
        }

        int[] dp = new int[budget + 1];
        for (int used = 0; used <= budget; used++) {
            if (node.cost() <= used) {
                int remaining = used - node.cost();
                dp[used] = Math.max(0, node.value() + childrenDp[remaining]);
            } else {
                dp[used] = 0;
            }
        }
        return dp;
    }

    private static int[] mergeValues(int[] left, int[] right, int budget) {
        int[] merged = new int[budget + 1];
        for (int usedLeft = 0; usedLeft <= budget; usedLeft++) {
            for (int usedRight = 0; usedLeft + usedRight <= budget; usedRight++) {
                merged[usedLeft + usedRight] = Math.max(
                        merged[usedLeft + usedRight],
                        left[usedLeft] + right[usedRight]
                );
            }
        }
        return merged;
    }

    private static List<String> reconstructSelection(TreeNode node, int budget) {
        List<String> selected = new ArrayList<>();
        if (node == null || budget < 0) {
            return selected;
        }

        int[] subtree = solveSubtree(node, budget);
        int maxWithNode = node.cost() <= budget ? node.value() + bestChildrenValue(node.children(), budget - node.cost()) : 0;
        if (maxWithNode <= 0 || subtree[budget] <= 0 || maxWithNode != subtree[budget]) {
            return selected;
        }

        selected.add(String.format("%s (%s, cost: $%d, val: %d)", node.name(), node.type(), node.cost(), node.value()));
        int remaining = budget - node.cost();
        for (TreeNode child : node.children()) {
            if (remaining <= 0) {
                break;
            }
            int[] childDp = solveSubtree(child, remaining);
            int chosenBudget = chooseBestBudget(childDp, remaining);
            if (chosenBudget > 0) {
                selected.addAll(reconstructSelection(child, chosenBudget));
                remaining -= chosenBudget;
            }
        }
        return selected;
    }

    private static int bestChildrenValue(List<TreeNode> children, int budget) {
        if (children == null || children.isEmpty()) {
            return 0;
        }

        int[] combined = new int[budget + 1];
        Arrays.fill(combined, 0);
        for (TreeNode child : children) {
            combined = mergeValues(combined, solveSubtree(child, budget), budget);
        }
        return combined[budget];
    }

    private static int chooseBestBudget(int[] dp, int budget) {
        int bestBudget = 0;
        int bestValue = -1;
        for (int b = 0; b <= budget; b++) {
            if (dp[b] > bestValue) {
                bestValue = dp[b];
                bestBudget = b;
            }
        }
        return bestValue > 0 ? bestBudget : 0;
    }
}
