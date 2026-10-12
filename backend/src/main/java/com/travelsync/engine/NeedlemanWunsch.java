package com.travelsync.engine;

/**
 * Needleman-Wunsch Dynamic Programming Algorithm
 *
 * Purpose: Optimal Global Sequence Alignment between two sequences end-to-end.
 * Time Complexity: O(N * M)
 * Space Complexity: O(N * M) for dynamic programming matrix and traceback
 * Scoring: Match = +2, Mismatch = -1, Gap = -2
 * Usage: Comprehensive global alignment and similarity scoring of full destination guides.
 */
public class NeedlemanWunsch {

    public static final int DEFAULT_MATCH_SCORE = 2;
    public static final int DEFAULT_MISMATCH_PENALTY = -1;
    public static final int DEFAULT_GAP_PENALTY = -2;

    public record AlignmentResult(
            int score,
            String alignedSeq1,
            String alignedSeq2,
            double identityPercentage,
            int matches,
            int mismatches,
            int gaps
    ) {}

    public static AlignmentResult align(String s1, String s2) {
        return align(s1, s2, DEFAULT_MATCH_SCORE, DEFAULT_MISMATCH_PENALTY, DEFAULT_GAP_PENALTY);
    }

    public static AlignmentResult align(String s1, String s2, int matchScore, int mismatchPenalty, int gapPenalty) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int n = s1.length();
        int m = s2.length();

        int[][] dp = new int[n + 1][m + 1];

        // Initialization
        for (int i = 0; i <= n; i++) dp[i][0] = i * gapPenalty;
        for (int j = 0; j <= m; j++) dp[0][j] = j * gapPenalty;

        // DP Recurrence
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int matchMismatch = dp[i - 1][j - 1] +
                        (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;
                dp[i][j] = Math.max(matchMismatch, Math.max(delete, insert));
            }
        }

        // Traceback from (n, m) to (0, 0)
        StringBuilder align1 = new StringBuilder();
        StringBuilder align2 = new StringBuilder();

        int i = n;
        int j = m;
        int matchCount = 0;
        int mismatchCount = 0;
        int gapCount = 0;

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0) {
                int scoreDiag = dp[i - 1][j - 1] +
                        (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);
                if (dp[i][j] == scoreDiag) {
                    align1.append(s1.charAt(i - 1));
                    align2.append(s2.charAt(j - 1));
                    if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                        matchCount++;
                    } else {
                        mismatchCount++;
                    }
                    i--;
                    j--;
                    continue;
                }
            }

            if (i > 0 && dp[i][j] == dp[i - 1][j] + gapPenalty) {
                align1.append(s1.charAt(i - 1));
                align2.append('-');
                gapCount++;
                i--;
            } else {
                align1.append('-');
                align2.append(s2.charAt(j - 1));
                gapCount++;
                j--;
            }
        }

        String res1 = align1.reverse().toString();
        String res2 = align2.reverse().toString();
        int totalAlignedLen = res1.length();
        double identity = totalAlignedLen == 0 ? 100.0 : (double) matchCount / totalAlignedLen * 100.0;

        return new AlignmentResult(dp[n][m], res1, res2, identity, matchCount, mismatchCount, gapCount);
    }
}
