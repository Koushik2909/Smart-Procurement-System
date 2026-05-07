package com.procurement.bid.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 10: Fraud & Collusion Detection
 * Uses Java 25 Advanced Streams for pattern analysis.
 */
@Service
public class FraudDetectionService {

    @Autowired
    private BidRepository bidRepository;

    private static final double COLLUSION_SIMILARITY_THRESHOLD = 0.05; // 5% difference
    private static final int MIN_BIDS_FOR_PATTERN = 2;

    /**
     * Detects bid collusion by finding bids on the same tender
     * whose financial scores are suspiciously close.
     */
    public List<String> detectBidCollusion(Long tenderId) {
        List<Bid> bids = bidRepository.findByTenderId(tenderId);

        List<String> alerts = new ArrayList<>();

        // Java 25 Advanced Streams: compare every pair of bids
        for (int i = 0; i < bids.size(); i++) {
            for (int j = i + 1; j < bids.size(); j++) {
                Bid b1 = bids.get(i);
                Bid b2 = bids.get(j);

                if (b1.getFinancialScore() != null && b2.getFinancialScore() != null) {
                    double diff = Math.abs(b1.getFinancialScore() - b2.getFinancialScore());
                    double avg = (b1.getFinancialScore() + b2.getFinancialScore()) / 2.0;
                    double similarity = (avg > 0) ? diff / avg : 0;

                    if (similarity < COLLUSION_SIMILARITY_THRESHOLD) {
                        alerts.add("COLLUSION_SUSPECTED: Vendor %d and Vendor %d have suspiciously similar scores (%.2f%% apart) on Tender %d"
                                .formatted(b1.getVendorId(), b2.getVendorId(), similarity * 100, tenderId));
                    }
                }
            }
        }

        if (alerts.isEmpty()) {
            alerts.add("No collusion patterns detected for Tender " + tenderId);
        }

        return alerts;
    }

    /**
     * Detects vendors who always bid together across multiple tenders
     * (cartel-like behavior).
     */
    public List<String> analyzeVendorPatterns() {
        List<Bid> allBids = bidRepository.findAll();

        // Group bids by tender: tenderId -> set of vendorIds
        Map<Long, Set<Long>> tenderToVendors = allBids.stream()
                .collect(Collectors.groupingBy(
                        Bid::getTenderId,
                        Collectors.mapping(Bid::getVendorId, Collectors.toSet())
                ));

        // Count how many times each vendor pair appears together
        Map<String, Long> pairFrequency = new LinkedHashMap<>();

        tenderToVendors.values().forEach(vendors -> {
            List<Long> vendorList = new ArrayList<>(vendors);
            for (int i = 0; i < vendorList.size(); i++) {
                for (int j = i + 1; j < vendorList.size(); j++) {
                    String key = Math.min(vendorList.get(i), vendorList.get(j)) + "-"
                            + Math.max(vendorList.get(i), vendorList.get(j));
                    pairFrequency.merge(key, 1L, Long::sum);
                }
            }
        });

        return pairFrequency.entrySet().stream()
                .filter(e -> e.getValue() >= MIN_BIDS_FOR_PATTERN)
                .map(e -> "PATTERN_ALERT: Vendor pair [%s] co-appeared in %d tenders — possible cartel behavior"
                        .formatted(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Detects price anomalies: bids far below or above the average for a tender.
     */
    public List<String> getFraudAlerts(Long tenderId) {
        List<Bid> bids = bidRepository.findByTenderId(tenderId).stream()
                .filter(b -> b.getFinancialScore() != null)
                .toList();

        if (bids.size() < 2) {
            return List.of("Insufficient bids to detect anomalies for Tender " + tenderId);
        }

        OptionalDouble avgOpt = bids.stream()
                .mapToDouble(Bid::getFinancialScore)
                .average();

        if (avgOpt.isEmpty()) return List.of("No financial scores available");

        double avg = avgOpt.getAsDouble();
        double stdDev = Math.sqrt(bids.stream()
                .mapToDouble(b -> Math.pow(b.getFinancialScore() - avg, 2))
                .average()
                .orElse(0.0));

        // Flag bids more than 2 standard deviations from mean
        return bids.stream()
                .filter(b -> Math.abs(b.getFinancialScore() - avg) > 2 * stdDev)
                .map(b -> "ANOMALY: Vendor %d's score %.2f deviates significantly from average %.2f (stddev=%.2f)"
                        .formatted(b.getVendorId(), b.getFinancialScore(), avg, stdDev))
                .collect(Collectors.toList());
    }
}
