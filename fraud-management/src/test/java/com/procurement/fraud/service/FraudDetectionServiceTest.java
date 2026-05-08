package com.procurement.fraud.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.domain.BidStatus;
import com.procurement.bid.repository.BidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.procurement.security.domain.BlocklistStatus;
import com.procurement.security.domain.FraudBlocklist;
import com.procurement.security.repository.FraudBlocklistRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudDetectionService Unit Tests")
class FraudDetectionServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private FraudBlocklistRepository fraudBlocklistRepository;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Bid bid1;
    private Bid bid2;
    private Bid bid3;

    @BeforeEach
    void setUp() {
        bid1 = new Bid();
        bid1.setId(1L); bid1.setTenderId(10L); bid1.setVendorId(1L);
        bid1.setStatus(BidStatus.EVALUATED); bid1.setFinancialScore(90.0);

        bid2 = new Bid();
        bid2.setId(2L); bid2.setTenderId(10L); bid2.setVendorId(2L);
        bid2.setStatus(BidStatus.EVALUATED); bid2.setFinancialScore(90.3); // Very close to bid1 — suspicious

        bid3 = new Bid();
        bid3.setId(3L); bid3.setTenderId(10L); bid3.setVendorId(3L);
        bid3.setStatus(BidStatus.EVALUATED); bid3.setFinancialScore(60.0); // Very low — anomaly
    }

    @Test
    @DisplayName("detectBidCollusion — nearly identical scores should flag collusion")
    void detectBidCollusion_SimilarScores_ShouldReturnAlert() {
        when(bidRepository.findByTenderId(10L)).thenReturn(List.of(bid1, bid2));

        List<String> alerts = fraudDetectionService.detectBidCollusion(10L);

        assertThat(alerts).anyMatch(a -> a.contains("COLLUSION_SUSPECTED"));
    }

    @Test
    @DisplayName("detectBidCollusion — diverse scores should not flag collusion")
    void detectBidCollusion_DiverseScores_ShouldReturnNoAlert() {
        when(bidRepository.findByTenderId(10L)).thenReturn(List.of(bid1, bid3));

        List<String> alerts = fraudDetectionService.detectBidCollusion(10L);

        assertThat(alerts).noneMatch(a -> a.contains("COLLUSION_SUSPECTED"));
        assertThat(alerts.getFirst()).contains("No collusion patterns detected");
    }

    @Test
    @DisplayName("getFraudAlerts — outlier scores should be flagged as anomalies")
    void getFraudAlerts_WithOutlier_ShouldFlagAnomaly() {
        // bid3 (60.0) is far from bid1 (90) and bid2 (90.3)
        when(bidRepository.findByTenderId(10L)).thenReturn(List.of(bid1, bid2, bid3));

        List<String> alerts = fraudDetectionService.getFraudAlerts(10L);

        assertThat(alerts).anyMatch(a -> a.contains("ANOMALY"));
    }

    @Test
    @DisplayName("getFraudAlerts — single bid should return insufficient data message")
    void getFraudAlerts_SingleBid_ShouldReturnInsufficientData() {
        when(bidRepository.findByTenderId(10L)).thenReturn(List.of(bid1));

        List<String> alerts = fraudDetectionService.getFraudAlerts(10L);

        assertThat(alerts.getFirst()).contains("Insufficient bids");
    }

    @Test
    @DisplayName("analyzeVendorPatterns — vendors co-appearing in 2+ tenders should be flagged")
    void analyzeVendorPatterns_CartelBehavior_ShouldReturnPatternAlert() {
        Bid tenderABid1 = new Bid(); tenderABid1.setTenderId(1L); tenderABid1.setVendorId(1L);
        Bid tenderABid2 = new Bid(); tenderABid2.setTenderId(1L); tenderABid2.setVendorId(2L);
        Bid tenderBBid1 = new Bid(); tenderBBid1.setTenderId(2L); tenderBBid1.setVendorId(1L);
        Bid tenderBBid2 = new Bid(); tenderBBid2.setTenderId(2L); tenderBBid2.setVendorId(2L);

        when(bidRepository.findAll()).thenReturn(List.of(tenderABid1, tenderABid2, tenderBBid1, tenderBBid2));

        List<String> patterns = fraudDetectionService.analyzeVendorPatterns();

        assertThat(patterns).anyMatch(p -> p.contains("PATTERN_ALERT"));
    }

    @Test
    @DisplayName("blockUser — should save a new FraudBlocklist entity")
    void blockUser_ShouldSaveEntity() {
        FraudBlocklist saved = new FraudBlocklist();
        saved.setUserId(99L);
        saved.setStatus(BlocklistStatus.BLOCKED);

        when(fraudBlocklistRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(fraudBlocklistRepository.save(any())).thenReturn(saved);

        FraudBlocklist result = fraudDetectionService.blockUser(99L, "Collusion");

        assertThat(result.getStatus()).isEqualTo(BlocklistStatus.BLOCKED);
        verify(fraudBlocklistRepository).save(any(FraudBlocklist.class));
    }

    @Test
    @DisplayName("unblockUser — should change status to ACTIVE")
    void unblockUser_ShouldSetStatusActive() {
        FraudBlocklist existing = new FraudBlocklist();
        existing.setUserId(99L);
        existing.setStatus(BlocklistStatus.BLOCKED);

        when(fraudBlocklistRepository.findByUserId(99L)).thenReturn(Optional.of(existing));
        when(fraudBlocklistRepository.save(any())).thenReturn(existing);

        FraudBlocklist result = fraudDetectionService.unblockUser(99L);

        assertThat(result.getStatus()).isEqualTo(BlocklistStatus.ACTIVE);
    }
}
