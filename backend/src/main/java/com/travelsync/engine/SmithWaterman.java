package com.travelsync.engine;

/**
 * Smith-Waterman Dynamic Programming Algorithm
 *
 * Purpose: Optimal Local Sequence Alignment. Finds the highest-scoring local
 *          similar region between two strings, ignoring dissimilar surrounding regions.
 * Time Complexity: O(N * M)
 * Space Complexity: O(N * M) for DP grid and traceback
 * Scoring: Match = +3, Mismatch = -2, Gap = -2, Lower bound = 0
 * Usage: Local travel description segment comparison and shared attraction motif detection.
 */
public class SmithWaterman {

    public static final int DEFAULT_MATCH_SCORE = 3;
    public static final int DEFAULT_MISMATCH_PENALTY = -2;
    public static final int DEFAULT_GAP_PENALTY = -2;

    public record LocalAlignmentResult(
            int maxScore,
            String alignedSeq1,
            String alignedSeq2,
            int startIdx1,
            int endIdx1,
            int startIdx2,
            int endIdx2,
            double identityPercentage
    ) {}

    public static LocalAlignmentResult align(String s1, String s2) {
        return align(s1, s2, DEFAULT_MATCH_SCORE, DEFAULT_MISMATCH_PENALTY, DEFAULT_GAP_PENALTY);
    }

    public static LocalAlignmentResult align(String s1, String s2, int matchScore, int mismatchPenalty, int gapPenalty) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        int n = s1.length();
        int m = s2.length();

        if (n == 0 || m == 0) {
            return new LocalAlignmentResult(0, "", "", 0, 0, 0, 0, 0.0);
        }

        int[][] dp = new int[n + 1][m + 1];
        int maxScore = 0;
        int maxI = 0;
        int maxJ = 0;

        // DP Recurrence with 0 lower bound
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int matchMismatch = dp[i - 1][j - 1] +
                        (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;

                int val = Math.max(0, Math.max(matchMismatch, Math.max(delete, insert)));
                dp[i][j] = val;

                if (val > maxScore) {
                    maxScore = val;
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        if (maxScore == 0) {
            return new LocalAlignmentResult(0, "", "", 0, 0, 0, 0, 0.0);
        }

        // Traceback from maximum scoring cell until reaching 0
        StringBuilder align1 = new StringBuilder();
        StringBuilder align2 = new StringBuilder();

        int i = maxI;
        int j = maxJ;
        int matches = 0;

        while (i > 0 && j > 0 && dp[i][j] > 0) {
            int scoreDiag = dp[i - 1][j - 1] +
                    (s1.charAt(i - 1) == s2.charAt(j - 1) ? matchScore : mismatchPenalty);

            if (dp[i][j] == scoreDiag) {
                align1.append(s1.charAt(i - 1));
                align2.append(s2.charAt(j - 1));
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) matches++;
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] + gapPenalty) {
                align1.append(s1.charAt(i - 1));
                align2.append('-');
                i--;
            } else {
                align1.append('-');
                align2.append(s2.charAt(j - 1));
                j--;
            }
        }

        String res1 = align1.reverse().toString();
        String res2 = align2.reverse().toString();
        int totalLen = res1.length();
        double identity = totalLen == 0 ? 0.0 : (double) matches / totalLen * 100.0;

        return new LocalAlignmentResult(maxScore, res1, res2, i, maxI - 1, j, maxJ - 1, identity);
    }
}
