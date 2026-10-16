package com.travelsync.service;

import com.travelsync.engine.LCP;
import com.travelsync.engine.NeedlemanWunsch;
import com.travelsync.engine.SmithWaterman;
import com.travelsync.model.Destination;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ComparisonService {

    private final DestinationService destinationService;

    public ComparisonService(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    public record DestinationComparisonResult(
            Destination destination1,
            Destination destination2,
            LCP.CommonSubstringResult longestCommonSubstring,
            List<String> sharedCategories,
            double geographicDistanceKm,
            String analysis
    ) {}

    public record DescriptionAlignmentResult(
            NeedlemanWunsch.AlignmentResult globalAlignment,
            SmithWaterman.LocalAlignmentResult localAlignment,
            String comparisonSummary
    ) {}

    public DestinationComparisonResult compareDestinations(String name1, String name2) {
        Destination d1 = destinationService.getDestinationByName(name1);
        Destination d2 = destinationService.getDestinationByName(name2);

        String desc1 = d1.getExtract() == null || d1.getExtract().isBlank()
                ? d1.getDescription() : d1.getExtract();
        String desc2 = d2.getExtract() == null || d2.getExtract().isBlank()
                ? d2.getDescription() : d2.getExtract();

        // 1. Suffix Array + Kasai's LCP for Longest Common Substring
        LCP.CommonSubstringResult lcs = LCP.findLongestCommonSubstring(desc1, desc2);

        // 2. Shared categories
        Set<String> catSet = new HashSet<>(d1.getCategories());
        catSet.retainAll(d2.getCategories());
        List<String> sharedCategories = new ArrayList<>(catSet);

        // 3. Geodesic distance between destinations
        double distanceKm = calculateHaversineKm(d1.getLatitude(), d1.getLongitude(), d2.getLatitude(), d2.getLongitude());

        String analysis = String.format("Comparing %s and %s: separated by %.1f km. Found %d shared categories. Suffix Array + LCP identified %d character common substring motif.",
                d1.getName(), d2.getName(), distanceKm, sharedCategories.size(), lcs.getLength());

        return new DestinationComparisonResult(d1, d2, lcs, sharedCategories, Math.round(distanceKm * 10.0) / 10.0, analysis);
    }

    public DescriptionAlignmentResult compareDescriptions(String text1, String text2) {
        if (text1 == null) text1 = "";
        if (text2 == null) text2 = "";

        // Truncate to reasonable length if comparing massive texts for responsiveness
        String sub1 = text1.length() > 300 ? text1.substring(0, 300) : text1;
        String sub2 = text2.length() > 300 ? text2.substring(0, 300) : text2;

        NeedlemanWunsch.AlignmentResult global = NeedlemanWunsch.align(sub1, sub2);
        SmithWaterman.LocalAlignmentResult local = SmithWaterman.align(sub1, sub2);

        String summary = String.format("Global identity: %.1f%% (score %d). Local alignment max score: %d (%.1f%% local identity).",
                global.identityPercentage(), global.score(), local.maxScore(), local.identityPercentage());

        return new DescriptionAlignmentResult(global, local, summary);
    }

    private double calculateHaversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
