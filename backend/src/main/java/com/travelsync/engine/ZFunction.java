package com.travelsync.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Z-Algorithm (Z-Function)
 *
 * Purpose: Computes the Z-array for a string S, where Z[i] is the length
 *          of the longest substring starting at i that is also a prefix of S.
 * Time Complexity: O(N + M) linear time using the [L, R] bounding window
 * Space Complexity: O(N + M) for the concatenated Z array
 * Usage: Pattern/text preprocessing, boundary inspection, and travel description search.
 */
public class ZFunction {

    /**
     * Computes the Z-array for string s.
     */
    public static int[] calculateZ(String s) {
        if (s == null || s.isEmpty()) {
            return new int[0];
        }

        int n = s.length();
        int[] z = new int[n];
        int l = 0;
        int r = 0;

        for (int i = 1; i < n; i++) {
            if (i <= r) {
                z[i] = Math.min(r - i + 1, z[i - l]);
            }
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] - 1 > r) {
                l = i;
                r = i + z[i] - 1;
            }
        }
        return z;
    }

    /**
     * Searches for pattern in text using the Z-Algorithm.
     * Formats: pattern + '$' + text
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

        // Use a unique delimiter that does not collide with common text
        char delimiter = '\u0001';
        String concat = pattern + delimiter + text;
        int[] z = calculateZ(concat);

        for (int i = m + 1; i < concat.length(); i++) {
            if (z[i] == m) {
                matches.add(i - m - 1);
            }
        }

        return matches;
    }
}
