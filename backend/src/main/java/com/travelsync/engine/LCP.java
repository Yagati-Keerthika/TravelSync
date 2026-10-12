package com.travelsync.engine;

/**
 * Longest Common Prefix (LCP) Array using Kasai's Algorithm
 *
 * Purpose: Computes the lengths of longest common prefixes between adjacent
 *          suffixes in the lexicographically sorted Suffix Array.
 * Time Complexity: O(N) linear time for Kasai's algorithm
 * Space Complexity: O(N) auxiliary for rank and LCP arrays
 * Usage: Destination text similarity, identifying shared travel themes, and
 *        finding the Longest Common Substring (LCS) between two guides.
 */
public class LCP {

    /**
     * Computes the LCP array for string s using its Suffix Array sa.
     * lcp[i] = length of LCP of sa[i] and sa[i-1] for i >= 1. lcp[0] = 0.
     */
    public static int[] computeLCP(String s, int[] sa) {
        if (s == null || sa == null || s.isEmpty() || sa.length == 0) {
            return new int[0];
        }

        int n = s.length();
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int[] lcp = new int[n];
        int h = 0;

        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            }
        }

        return lcp;
    }

    public static class CommonSubstringResult {
        private final String longestCommonSubstring;
        private final int length;
        private final int startIndexText1;
        private final int startIndexText2;

        public CommonSubstringResult(String longestCommonSubstring, int length, int startIndexText1, int startIndexText2) {
            this.longestCommonSubstring = longestCommonSubstring;
            this.length = length;
            this.startIndexText1 = startIndexText1;
            this.startIndexText2 = startIndexText2;
        }

        public String getLongestCommonSubstring() { return longestCommonSubstring; }
        public int getLength() { return length; }
        public int getStartIndexText1() { return startIndexText1; }
        public int getStartIndexText2() { return startIndexText2; }
    }

    /**
     * Finds the Longest Common Substring between two destination descriptions using Suffix Array + Kasai's LCP.
     */
    public static CommonSubstringResult findLongestCommonSubstring(String s1, String s2) {
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            return new CommonSubstringResult("", 0, -1, -1);
        }

        int len1 = s1.length();
        // Concatenate using sentinel delimiters distinct from input
        String combined = s1 + "#" + s2 + "$";
        int totalLen = combined.length();

        SuffixArray sa = new SuffixArray(combined);
        int[] saArr = sa.getArray();
        int[] lcp = computeLCP(combined, saArr);

        int maxLen = 0;
        int bestSaIdx = -1;

        for (int i = 1; i < totalLen; i++) {
            int p1 = saArr[i];
            int p2 = saArr[i - 1];

            // Verify the two suffixes come from different original strings
            boolean fromDifferent = (p1 < len1 && p2 > len1) || (p1 > len1 && p2 < len1);
            if (fromDifferent && lcp[i] > maxLen) {
                maxLen = lcp[i];
                bestSaIdx = i;
            }
        }

        if (maxLen > 0 && bestSaIdx != -1) {
            int startInCombined = saArr[bestSaIdx];
            String match = combined.substring(startInCombined, startInCombined + maxLen);

            int start1 = s1.indexOf(match);
            int start2 = s2.indexOf(match);

            return new CommonSubstringResult(match, maxLen, start1, start2);
        }

        return new CommonSubstringResult("", 0, -1, -1);
    }
}
