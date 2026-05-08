package com.procurement.bid.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.domain.BidEvaluation;
import com.procurement.bid.domain.BidStatus;
import com.procurement.bid.dto.BidInput;
import com.procurement.bid.repository.BidEvaluationRepository;
import com.procurement.bid.repository.BidRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import com.procurement.security.domain.BlocklistStatus;
import com.procurement.security.domain.FraudBlocklist;
import com.procurement.security.repository.FraudBlocklistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidService Unit Tests")
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private BidEvaluationRepository bidEvaluationRepository;

    @Mock
    private FraudBlocklistRepository fraudBlocklistRepository;

    @InjectMocks
    private BidService bidService;

    private Bid submittedBid;

    @BeforeEach
    void setUp() {
        submittedBid = new Bid();
        submittedBid.setId(1L);
        submittedBid.setTenderId(10L);
        submittedBid.setVendorId(2L);
        submittedBid.setStatus(BidStatus.SUBMITTED);
        submittedBid.setTechnicalProposalUrl("https://docs.example.com/proposal.pdf");
        submittedBid.setEncryptedFinancialProposal("ENC_ABC123");
    }

    @Test
    @DisplayName("submitBid — should save bid if vendor is NOT blocklisted")
    void submitBid_ShouldReturnSubmittedBid() {
        BidInput input = new BidInput(10L, "url", "enc");
        when(fraudBlocklistRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(bidRepository.save(any(Bid.class))).thenReturn(submittedBid);

        Bid result = bidService.submitBid(input, 2L);

        assertThat(result.getStatus()).isEqualTo(BidStatus.SUBMITTED);
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    @DisplayName("submitBid — should throw Exception if vendor is BLOCKED")
    void submitBid_BlockedVendor_ShouldThrowException() {
        BidInput input = new BidInput(10L, "url", "enc");
        FraudBlocklist blocked = new FraudBlocklist();
        blocked.setStatus(BlocklistStatus.BLOCKED);
        when(fraudBlocklistRepository.findByUserId(2L)).thenReturn(Optional.of(blocked));

        assertThatThrownBy(() -> bidService.submitBid(input, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied: Vendor is blocklisted");
    }

    @Test
    @DisplayName("assignEvaluator — vendor cannot evaluate own bid")
    void assignEvaluator_ConflictOfInterest_ShouldThrowException() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(submittedBid));

        // Vendor ID is 2, Evaluator ID is 2
        assertThatThrownBy(() -> bidService.assignEvaluator(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conflict of Interest");
    }

    @Test
    @DisplayName("finalizeEvaluation — should calculate weighted score based on evaluator inputs")
    void finalizeEvaluation_ShouldCalculateWeightedScore() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(submittedBid));

        BidEvaluation eval1 = new BidEvaluation(); eval1.setTechnicalScore(80.0); eval1.setFinancialScore(90.0);
        BidEvaluation eval2 = new BidEvaluation(); eval2.setTechnicalScore(90.0); eval2.setFinancialScore(100.0);
        
        when(bidEvaluationRepository.findByBidId(1L)).thenReturn(List.of(eval1, eval2));
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bid result = bidService.finalizeEvaluation(1L);

        // Avg Tech = 85.0, Avg Fin = 95.0
        // w1 = 0.7, w2 = 0.3
        // finalScore = (0.7 * 85) + (0.3 * 95) = 59.5 + 28.5 = 88.0
        assertThat(result.getStatus()).isEqualTo(BidStatus.EVALUATED);
        assertThat(result.getTechnicalScore()).isEqualTo(85.0);
        assertThat(result.getFinancialScore()).isEqualTo(95.0);
        assertThat(result.getFinalScore()).isEqualTo(88.0);
    }
}
