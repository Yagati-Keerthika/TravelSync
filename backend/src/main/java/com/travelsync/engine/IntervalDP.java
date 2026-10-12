package com.travelsync.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Interval Dynamic Programming (Weighted Activity Scheduling)
 *
 * Purpose: Selects the mutually non-overlapping set of tourist activities that
 *          maximizes total tourist enjoyment/priority value within a daily time window.
 * Time Complexity: O(N log N) using sorting and binary search predecessor lookup
 * Space Complexity: O(N) for DP memoization and schedule backtracking
 * Usage: Activity planner optimizing day schedules (Temple, Museum, Beach, Hiking).
 */
public class IntervalDP {

    public record Activity(
            String id,
            String name,
            int startMinutes,
            int endMinutes,
            double value,
            String category
    ) {
        public String formatTimeRange() {
            int startH = 8 + (startMinutes / 60);
            int startM = startMinutes % 60;
            int endH = 8 + (endMinutes / 60);
            int endM = endMinutes % 60;
            return String.format("%02d:%02d - %02d:%02d", startH, startM, endH, endM);
        }
    }

    public record ScheduleResult(
            double totalValue,
            List<Activity> selectedActivities,
            int totalActivitiesCount,
            String complexity
    ) {}

    /**
     * Finds the maximum value set of non-overlapping activities using Interval DP.
     */
    public static ScheduleResult optimizeSchedule(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return new ScheduleResult(0.0, List.of(), 0, "O(1)");
        }

        // 1. Sort activities by end time
        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingInt(Activity::endMinutes));

        int n = sorted.size();
        double[] dp = new double[n + 1];
        int[] prev = new int[n + 1]; // predecessor index in sorted list

        // Find p[i]: latest non-overlapping activity before sorted[i]
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = findLatestNonOverlapping(sorted, i);
        }

        // DP Recurrence: dp[i] = max(dp[i - 1], sorted[i - 1].value + dp[p[i - 1] + 1])
        boolean[] take = new boolean[n + 1];

        for (int i = 1; i <= n; i++) {
            double excludeVal = dp[i - 1];
            double includeVal = sorted.get(i - 1).value() + (p[i - 1] != -1 ? dp[p[i - 1] + 1] : 0.0);

            if (includeVal > excludeVal) {
                dp[i] = includeVal;
                take[i] = true;
            } else {
                dp[i] = excludeVal;
                take[i] = false;
            }
        }

        // Traceback selected activities
        List<Activity> selected = new ArrayList<>();
        int curr = n;
        while (curr > 0) {
            if (take[curr]) {
                selected.add(sorted.get(curr - 1));
                curr = p[curr - 1] + 1;
            } else {
                curr--;
            }
        }

        Collections.reverse(selected);
        String complexity = String.format("O(%d log %d) = %d ops", n, n, (int) (n * (Math.log(n) / Math.log(2))));

        return new ScheduleResult(dp[n], selected, selected.size(), complexity);
    }

    /**
     * Binary search to find the rightmost activity j < i such that sorted[j].endMinutes <= sorted[i].startMinutes.
     */
    private static int findLatestNonOverlapping(List<Activity> sorted, int i) {
        int low = 0;
        int high = i - 1;
        int targetStart = sorted.get(i).startMinutes();
        int ans = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (sorted.get(mid).endMinutes() <= targetStart) {
                ans = mid;
                low = mid + 1; // Try to find a later compatible activity
            } else {
                high = mid - 1;
            }
        }
        return ans;
    }
}
