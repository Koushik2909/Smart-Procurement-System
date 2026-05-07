package com.procurement.tender.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.repository.BidRepository;
import com.procurement.tender.domain.Tender;
import com.procurement.tender.domain.TenderStatus;
import com.procurement.tender.dto.ProcurementInsightsDTO;
import com.procurement.tender.dto.TenderAnalyticsDTO;
import com.procurement.tender.dto.VendorPerformanceDTO;
import com.procurement.tender.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private BidRepository bidRepository;

    public TenderAnalyticsDTO getTenderAnalytics() {
        List<Tender> all = tenderRepository.findAll();

        Map<TenderStatus, Long> counts = all.stream()
                .collect(Collectors.groupingBy(Tender::getStatus, () -> new EnumMap<>(TenderStatus.class), Collectors.counting()));

        return new TenderAnalyticsDTO(
                counts.getOrDefault(TenderStatus.DRAFT, 0L),
                counts.getOrDefault(TenderStatus.PUBLISHED, 0L),
                counts.getOrDefault(TenderStatus.OPEN, 0L),
                counts.getOrDefault(TenderStatus.CLOSED, 0L),
                counts.getOrDefault(TenderStatus.EVALUATION, 0L),
                counts.getOrDefault(TenderStatus.AWARDED, 0L),
                counts.getOrDefault(TenderStatus.CANCELLED, 0L)
        );
    }

    public VendorPerformanceDTO getVendorPerformance(Long vendorId) {
        List<Bid> bids = bidRepository.findByVendorId(vendorId);

        double avgTech = bids.stream()
                .filter(b -> b.getTechnicalScore() != null)
                .mapToDouble(Bid::getTechnicalScore)
                .average().orElse(0.0);

        double avgFin = bids.stream()
                .filter(b -> b.getFinancialScore() != null)
                .mapToDouble(Bid::getFinancialScore)
                .average().orElse(0.0);

        // Final weighted score: 40% technical + 60% financial
        double finalScore = (0.4 * avgTech) + (0.6 * avgFin);

        return new VendorPerformanceDTO(avgTech, avgFin, finalScore, bids.size());
    }

    public ProcurementInsightsDTO getProcurementInsights() {
        List<Tender> all = tenderRepository.findAll();
        return new ProcurementInsightsDTO(
                all.size(),
                all.stream().filter(t -> t.getStatus() == TenderStatus.OPEN).count(),
                all.stream().filter(t -> t.getStatus() == TenderStatus.AWARDED).count(),
                all.stream().filter(t -> t.getStatus() == TenderStatus.CANCELLED).count(),
                LocalDateTime.now().toString()
        );
    }
}
