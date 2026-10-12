package com.travelsync.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp String Matching Algorithm
 *
 * Purpose: Rolling-hash based substring matching. Uses polynomial hashing
 *          to quickly eliminate mismatched positions, followed by character
 *          verification on hash collisions.
 * Time Complexity: Average O(N + M), Worst-case O(N * M)
 * Space Complexity: O(1) auxiliary
 * Usage: Booking/reservation reference code matching and ticket confirmation search.
 */
public class RabinKarp {

    private static final long PRIME = 1_000_000_007L;
    private static final long RADIX = 256L;

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

        long h = 1;
        for (int i = 0; i < m - 1; i++) {
            h = (h * RADIX) % PRIME;
        }

        long pHash = 0;
        long tHash = 0;

        for (int i = 0; i < m; i++) {
            pHash = (RADIX * pHash + pattern.charAt(i)) % PRIME;
            tHash = (RADIX * tHash + text.charAt(i)) % PRIME;
        }

        for (int i = 0; i <= n - m; i++) {
            if (pHash == tHash) {
                // Verify characters to handle hash collisions
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    matches.add(i);
                }
            }

            // Calculate rolling hash for next window
            if (i < n - m) {
                tHash = (RADIX * (tHash - text.charAt(i) * h) + text.charAt(i + m)) % PRIME;
                if (tHash < 0) {
                    tHash = tHash + PRIME;
                }
            }
        }

        return matches;
    }
}
