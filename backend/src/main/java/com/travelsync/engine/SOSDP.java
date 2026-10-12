package com.travelsync.engine;

import java.util.Arrays;
import java.util.List;

/**
 * Sum Over Subsets Dynamic Programming (SOS DP)
 *
 * Purpose: Computes subset and superset aggregations over boolean preference
 *          feature vectors in O(K * 2^K) time instead of O(3^K) or O(4^K).
 * Time Complexity: O(K * 2^K) where K is number of bit features
 * Space Complexity: O(2^K) for the bitmask table
 * Usage: Personalized travel recommendation engine matching traveler interest masks.
 */
public class SOSDP {

    public record SosResult(
            int[] subsetSums,
            int[] supersetSums,
            int totalBits,
            String complexity
    ) {}

    /**
     * Computes the SOS DP table for an array of counts/values indexed by mask.
     *
     * @param counts Array of length 2^k containing count of destinations with exact mask
     * @param totalBits Number of bit dimensions (e.g. 8 for 256 states)
     * @return SosResult containing subset and superset aggregates
     */
    public static SosResult compute(int[] counts, int totalBits) {
        int totalMasks = 1 << totalBits;
        int[] subDp = Arrays.copyOf(counts, totalMasks);
        int[] superDp = Arrays.copyOf(counts, totalMasks);

        // 1. Subset DP: F[mask] = sum_{submask <= mask} counts[submask]
        for (int i = 0; i < totalBits; i++) {
            for (int mask = 0; mask < totalMasks; mask++) {
                if ((mask & (1 << i)) != 0) {
                    subDp[mask] += subDp[mask ^ (1 << i)];
                }
            }
        }

        // 2. Superset DP: G[mask] = sum_{mask <= supermask} counts[supermask]
        for (int i = 0; i < totalBits; i++) {
            for (int mask = totalMasks - 1; mask >= 0; mask--) {
                if ((mask & (1 << i)) == 0) {
                    superDp[mask] += superDp[mask | (1 << i)];
                }
            }
        }

        String complexity = String.format("O(%d * 2^%d) = %d operations", totalBits, totalBits, totalBits * totalMasks);
        return new SosResult(subDp, superDp, totalBits, complexity);
    }

    /**
     * Calculates compatibility score between traveler mask and destination mask.
     */
    public static double calculateMatchScore(int travelerMask, int destinationMask) {
        if (travelerMask == 0) return 0.5; // neutral

        int intersection = travelerMask & destinationMask;
        int union = travelerMask | destinationMask;

        int sharedBits = Integer.bitCount(intersection);
        int travelerBits = Integer.bitCount(travelerMask);
        int unionBits = Integer.bitCount(union);

        // Jaccard similarity weighted by traveler coverage
        double recall = (double) sharedBits / travelerBits;
        double jaccard = unionBits == 0 ? 0.0 : (double) sharedBits / unionBits;

        return (recall * 0.7) + (jaccard * 0.3);
    }
}
