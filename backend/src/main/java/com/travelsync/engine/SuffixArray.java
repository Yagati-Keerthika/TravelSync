package com.travelsync.engine;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Suffix Array Construction
 *
 * Purpose: Constructs an array of integers representing the starting indices of the
 *          lexicographically sorted suffixes of a string.
 * Time Complexity: O(N log^2 N) using prefix doubling, or O(N log N)
 * Space Complexity: O(N) auxiliary
 * Usage: Foundation for fast substring queries, text comparison, and LCP calculation.
 */
public class SuffixArray {

    private final String text;
    private final int[] sa;

    public SuffixArray(String text) {
        this.text = text != null ? text : "";
        this.sa = buildSuffixArray(this.text);
    }

    private static int[] buildSuffixArray(String s) {
        int n = s.length();
        if (n == 0) return new int[0];

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;

        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = s.charAt(i);

        int[] tempRank = new int[n];

        for (int k = 1; k < n; k *= 2) {
            final int len = k;
            final int[] currentRank = rank;

            Comparator<Integer> comp = (a, b) -> {
                if (currentRank[a] != currentRank[b]) {
                    return Integer.compare(currentRank[a], currentRank[b]);
                }
                int rankA = (a + len < n) ? currentRank[a + len] : -1;
                int rankB = (b + len < n) ? currentRank[b + len] : -1;
                return Integer.compare(rankA, rankB);
            };

            Arrays.sort(order, comp);

            tempRank[order[0]] = 0;
            for (int i = 1; i < n; i++) {
                tempRank[order[i]] = tempRank[order[i - 1]] +
                        (comp.compare(order[i - 1], order[i]) < 0 ? 1 : 0);
            }
            System.arraycopy(tempRank, 0, rank, 0, n);
            if (rank[order[n - 1]] == n - 1) break;
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = order[i];
        return result;
    }

    public int[] getArray() {
        return sa;
    }

    public String getText() {
        return text;
    }

    public int length() {
        return sa.length;
    }

    public int get(int i) {
        return sa[i];
    }
}
