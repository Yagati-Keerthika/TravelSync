package com.travelsync.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt (KMP) String Matching Algorithm
 *
 * Purpose: Linear-time exact pattern matching avoiding redundant backtracking
 *          by preprocessing the pattern into a Longest Prefix Suffix (LPS) array.
 * Time Complexity: Preprocessing O(M), Matching O(N), Overall O(N + M)
 * Space Complexity: O(M) for the LPS table
 * Usage: Fast keyword search across destination descriptions, attractions, and guide extracts.
 */
public class KMP {

    /**
     * Precomputes the Longest Proper Prefix which is also Suffix (LPS) array.
     */
    public static int[] computeLPS(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new int[0];
        }
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    /**
     * Searches for all occurrences of pattern in text using KMP.
     *
     * @param text The source text to search in
     * @param pattern The keyword/pattern to find
     * @return 0-indexed starting positions of all matches
     */
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

        int[] lps = computeLPS(pattern);
        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == m) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < n && text.charAt(i) != pattern.charAt(j)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return matches;
    }
}
