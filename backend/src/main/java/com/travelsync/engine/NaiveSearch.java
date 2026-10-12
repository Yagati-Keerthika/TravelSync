package com.travelsync.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Naive String Matching Algorithm
 *
 * Purpose: Baseline exact substring matching by brute-force comparison.
 * Time Complexity: Best O(N), Worst O((N - M + 1) * M)
 * Space Complexity: O(1) auxiliary
 * Usage: Initial simple text verification and algorithmic baseline benchmark.
 */
public class NaiveSearch {

    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || n < m) {
            return matches;
        }

        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) {
                matches.add(i);
            }
        }

        return matches;
    }
}
