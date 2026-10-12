package com.travelsync.engine;

import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DsaEngineTest {

    @BeforeAll
    public static void printBannerStart() {
        System.out.println("\n=================================");
        System.out.println("       TRAVELSYNC TESTS");
        System.out.println("=================================");
    }

    @AfterAll
    public static void printBannerEnd() {
        System.out.println("=================================");
        System.out.println("      ALL TESTS COMPLETED");
        System.out.println("=================================\n");
    }

    @Test
    @Order(1)
    public void testNaiveSearch() {
        // Normal & multiple matches ("PARIS IN PARIS" -> index 0 and 9)
        assertEquals(List.of(0, 9), NaiveSearch.search("PARIS IN PARIS", "PARIS"));
        // No match
        assertTrue(NaiveSearch.search("Tokyo, Japan", "Kyoto").isEmpty());
        // Empty pattern & empty text
        assertTrue(NaiveSearch.search("", "Delhi").isEmpty());
        assertTrue(NaiveSearch.search("Delhi", "").isEmpty());
        // Single-character
        assertEquals(List.of(1), NaiveSearch.search("MUMBAI", "U"));
        System.out.println("Naive Search: PASS");
    }

    @Test
    @Order(2)
    public void testKMP() {
        // Normal & multiple matches
        assertEquals(List.of(0, 10), KMP.search("MANALI IN MANALI VALLEY", "MANALI"));
        // Overlapping match in "AAAA" for "AA" -> [0, 1, 2]
        assertEquals(List.of(0, 1, 2), KMP.search("AAAA", "AA"));
        // No match
        assertTrue(KMP.search("Singapore Marina Bay", "Bangkok").isEmpty());
        // Empty & single character
        assertTrue(KMP.search("Rome", "").isEmpty());
        assertEquals(List.of(0, 1, 2), KMP.search("AAA", "A"));
        System.out.println("KMP: PASS");
    }

    @Test
    @Order(3)
    public void testZFunction() {
        // Normal match
        assertEquals(List.of(12), ZFunction.search("Explore the Jaipur pink city", "Jaipur"));
        // Multiple matches
        assertEquals(List.of(0, 7), ZFunction.search("BEACHESBEACH", "BEACH"));
        // No match
        assertTrue(ZFunction.search("Goa Sunset", "Sunrise").isEmpty());
        // Empty inputs
        assertTrue(ZFunction.search("", "test").isEmpty());
        assertTrue(ZFunction.search("test", "").isEmpty());
        System.out.println("Z-Function: PASS");
    }

    @Test
    @Order(4)
    public void testRabinKarp() {
        // Booking code search
        String bookingLog = "BOOKING_REF_TS-9842_CONFIRMED, REF_TS-9842_PAID";
        assertEquals(List.of(12, 35), RabinKarp.search(bookingLog, "TS-9842"));
        // No match
        assertTrue(RabinKarp.search(bookingLog, "TS-9999").isEmpty());
        // Empty & single char
        assertTrue(RabinKarp.search("", "XYZ").isEmpty());
        assertEquals(List.of(0, 1, 2), RabinKarp.search("ZZZ", "Z"));
        System.out.println("Rabin-Karp: PASS");
    }

    @Test
    @Order(5)
    public void testAhoCorasick() {
        List<String> keywords = List.of("beach", "temple", "nature", "mountain");
        String travelText = "Visit the ancient temple near the golden beach surrounded by pure nature and tall mountain peaks.";

        Map<String, List<Integer>> results = AhoCorasick.searchAll(travelText, keywords);
        assertTrue(results.containsKey("beach"));
        assertTrue(results.containsKey("temple"));
        assertTrue(results.containsKey("nature"));
        assertTrue(results.containsKey("mountain"));

        // No match
        Map<String, List<Integer>> noMatch = AhoCorasick.searchAll("Urban concrete city", List.of("jungle", "safari"));
        assertTrue(noMatch.isEmpty());
        System.out.println("Aho-Corasick: PASS");
    }

    @Test
    @Order(6)
    public void testEditDistance() {
        // Typo correction: Hyderbad -> Hyderabad
        EditDistance.Result r1 = EditDistance.compute("Hyderbad", "Hyderabad");
        assertEquals(1, r1.distance());
        assertTrue(r1.similarity() > 0.88);

        // Identical
        assertEquals(0, EditDistance.getDistance("Tokyo", "Tokyo"));

        // Completely different
        assertEquals(5, EditDistance.getDistance("Paris", "Tokyo"));

        // Empty string edge case
        assertEquals(6, EditDistance.getDistance("", "Mumbai"));
        System.out.println("Edit Distance: PASS");
    }

    @Test
    @Order(7)
    public void testSuffixArray() {
        String s = "BANANA";
        SuffixArray sa = new SuffixArray(s);
        int[] arr = sa.getArray();
        assertEquals(6, arr.length);
        // Suffixes sorted: A (5), ANA (3), ANANA (1), BANANA (0), NA (4), NANA (2)
        assertArrayEquals(new int[]{5, 3, 1, 0, 4, 2}, arr);

        // Single-character
        SuffixArray saSingle = new SuffixArray("A");
        assertArrayEquals(new int[]{0}, saSingle.getArray());
        System.out.println("Suffix Array: PASS");
    }

    @Test
    @Order(8)
    public void testLCP() {
        String s = "BANANA";
        SuffixArray sa = new SuffixArray(s);
        int[] lcp = LCP.computeLCP(s, sa.getArray());
        // LCP values for BANANA: [0, 1, 3, 0, 0, 2]
        assertEquals(6, lcp.length);
        assertEquals(0, lcp[0]);
        assertEquals(1, lcp[1]); // LCP("A", "ANA") = 1
        assertEquals(3, lcp[2]); // LCP("ANA", "ANANA") = 3

        // Longest Common Substring between 2 destination descriptions
        String desc1 = "Manali is famous for scenic Himalayan snow peaks and apple orchards";
        String desc2 = "Shimla features historic ridge walks and scenic Himalayan vistas";
        LCP.CommonSubstringResult lcs = LCP.findLongestCommonSubstring(desc1, desc2);
        assertTrue(lcs.getLength() > 0);
        assertTrue(lcs.getLongestCommonSubstring().contains("scenic Himalayan"));
        System.out.println("LCP: PASS");
    }

    @Test
    @Order(9)
    public void testNeedlemanWunsch() {
        // Global alignment
        NeedlemanWunsch.AlignmentResult res = NeedlemanWunsch.align("PARIS", "PARIS");
        assertEquals(10, res.score()); // 5 * 2
        assertEquals("PARIS", res.alignedSeq1());
        assertEquals("PARIS", res.alignedSeq2());
        assertEquals(100.0, res.identityPercentage());

        // Gapped alignment
        NeedlemanWunsch.AlignmentResult gapped = NeedlemanWunsch.align("PARIS", "PARS");
        assertTrue(gapped.gaps() >= 1);
        System.out.println("Needleman-Wunsch: PASS");
    }

    @Test
    @Order(10)
    public void testSmithWaterman() {
        // Local similarity motif
        String s1 = "PREFIX_BEACH_PARADISE_SUFFIX";
        String s2 = "START_BEACH_PARADISE_END";
        SmithWaterman.LocalAlignmentResult local = SmithWaterman.align(s1, s2);
        assertTrue(local.maxScore() > 0);
        assertTrue(local.alignedSeq1().contains("BEACH_PARADISE"));
        System.out.println("Smith-Waterman: PASS");
    }

    @Test
    @Order(11)
    public void testBitmaskDP() {
        // 4 Cities: City 0 -> City 1 -> City 2 -> City 3
        double[][] matrix = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
        };

        BitmaskDP.TourResult tour = BitmaskDP.solveOptimalTour(matrix, 0);
        assertEquals(4, tour.optimalOrder().size());
        assertEquals(0, tour.optimalOrder().get(0));
        // Path 0 -> 2 (15) -> 3 (30) -> 1 (25) = 70 OR 0 -> 1 (10) -> 3 (25) -> 2 (30) = 65
        assertTrue(tour.minCost() <= 70.0);
        System.out.println("Bitmask DP: PASS");
    }

    @Test
    @Order(12)
    public void testIntervalDP() {
        List<IntervalDP.Activity> activities = List.of(
                new IntervalDP.Activity("A1", "Morning Temple Tour", 0, 120, 40.0, "Heritage"),
                new IntervalDP.Activity("A2", "Extended Heritage Walk", 30, 180, 50.0, "Heritage"), // overlaps A1 & A3
                new IntervalDP.Activity("A3", "Museum Visit", 130, 210, 35.0, "Culture"),
                new IntervalDP.Activity("A4", "Sunset Beach Walk", 240, 360, 60.0, "Nature")
        );

        IntervalDP.ScheduleResult result = IntervalDP.optimizeSchedule(activities);
        // Optimal should pick A1 (40) + A3 (35) + A4 (60) = 135 (better than A2 (50) + A4 (60) = 110)
        assertEquals(135.0, result.totalValue());
        assertEquals(3, result.selectedActivities().size());
        System.out.println("Interval DP: PASS");
    }

    @Test
    @Order(13)
    public void testTreeDP() {
        // Root: Trip ($100, val: 50)
        TreeDP.TreeNode trip = new TreeDP.TreeNode(0, "Rajasthan Tour", "TRIP", 100, 50);

        // Day 1 ($50, val: 30) with 2 Activities
        TreeDP.TreeNode day1 = new TreeDP.TreeNode(1, "Day 1: Jaipur City", "DAY", 50, 30);
        TreeDP.TreeNode act1 = new TreeDP.TreeNode(2, "Amber Fort Hike", "ACTIVITY", 20, 25);
        TreeDP.TreeNode act2 = new TreeDP.TreeNode(3, "Hawa Mahal Photography", "ACTIVITY", 10, 15);
        day1.children().addAll(List.of(act1, act2));

        // Day 2 ($60, val: 40) with 1 Activity
        TreeDP.TreeNode day2 = new TreeDP.TreeNode(4, "Day 2: Jodhpur Desert", "DAY", 60, 40);
        TreeDP.TreeNode act3 = new TreeDP.TreeNode(5, "Camel Safari", "ACTIVITY", 30, 35);
        day2.children().add(act3);

        trip.children().addAll(List.of(day1, day2));

        // With Budget $180: Trip ($100) + Day 1 ($50) + Amber Fort ($20) + Hawa Mahal ($10) = $180, Val = 50 + 30 + 25 + 15 = 120
        TreeDP.TreeOptimizationResult res = TreeDP.optimize(trip, 180);
        assertEquals(120, res.maxValue());
        assertEquals(180, res.totalCostUsed());
        System.out.println("Tree DP: PASS");
    }

    @Test
    @Order(14)
    public void testSOSDP() {
        // 3 preference bits: Beach (1), Historical (2), Nature (4)
        // Masks 0..7
        int totalBits = 3;
        int[] counts = new int[1 << totalBits];
        // 2 beach destinations (mask 1)
        counts[1] = 2;
        // 3 historical destinations (mask 2)
        counts[2] = 3;
        // 1 beach + nature destination (mask 1 | 4 = 5)
        counts[5] = 1;
        // 2 beach + historical + nature destinations (mask 1 | 2 | 4 = 7)
        counts[7] = 2;

        SOSDP.SosResult sos = SOSDP.compute(counts, totalBits);

        // Subset sum for mask 5 (subsets 0, 1, 4, 5) -> counts[1](2) + counts[5](1) = 3
        assertEquals(3, sos.subsetSums()[5]);

        // Superset sum for mask 1 (Beach) -> mask 1, 3, 5, 7 -> 2 + 0 + 1 + 2 = 5 destinations having beach
        assertEquals(5, sos.supersetSums()[1]);

        System.out.println("SOS DP: PASS");
    }
}
