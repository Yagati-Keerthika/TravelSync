package com.travelsync.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Edit Distance (Wagner-Fischer Dynamic Programming Algorithm)
 *
 * Purpose: Computes the minimum number of single-character edits (insertions,
 *          deletions, substitutions) required to transform string S1 into S2.
 * Time Complexity: O(N * M)
 * Space Complexity: O(N * M) for full matrix with operation backtracking
 * Usage: Typo correction and fuzzy query auto-suggestions for destination names.
 */
public class EditDistance {

    public enum OpType { MATCH, INSERT, DELETE, SUBSTITUTE }

    public record EditOp(OpType type, char charA, char charB, int posA, int posB) {}

    public record Result(
            int distance,
            double similarity,
            List<EditOp> operations
    ) {}

    public static Result compute(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int n = s1.length();
        int m = s2.length();

        int[][] dp = new int[n + 1][m + 1];

        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insertCost = dp[i][j - 1] + 1;
                    int deleteCost = dp[i - 1][j] + 1;
                    int subCost = dp[i - 1][j - 1] + 1;
                    dp[i][j] = Math.min(insertCost, Math.min(deleteCost, subCost));
                }
            }
        }

        int distance = dp[n][m];
        int maxLen = Math.max(n, m);
        double similarity = maxLen == 0 ? 1.0 : (1.0 - (double) distance / maxLen);

        // Backtrack edit operations
        List<EditOp> ops = new ArrayList<>();
        int i = n, j = m;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && s1.charAt(i - 1) == s2.charAt(j - 1)) {
                ops.add(new EditOp(OpType.MATCH, s1.charAt(i - 1), s2.charAt(j - 1), i - 1, j - 1));
                i--;
                j--;
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                ops.add(new EditOp(OpType.SUBSTITUTE, s1.charAt(i - 1), s2.charAt(j - 1), i - 1, j - 1));
                i--;
                j--;
            } else if (j > 0 && dp[i][j] == dp[i][j - 1] + 1) {
                ops.add(new EditOp(OpType.INSERT, '-', s2.charAt(j - 1), i, j - 1));
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                ops.add(new EditOp(OpType.DELETE, s1.charAt(i - 1), '-', i - 1, j));
                i--;
            } else {
                break;
            }
        }
        Collections.reverse(ops);

        return new Result(distance, similarity, ops);
    }

    /**
     * Fast distance without full path reconstruction.
     */
    public static int getDistance(String s1, String s2) {
        return compute(s1, s2).distance();
    }
}
