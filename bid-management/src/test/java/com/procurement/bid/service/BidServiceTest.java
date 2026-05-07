package com.procurement.bid.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.domain.BidStatus;
import com.procurement.bid.dto.BidInput;
import com.procurement.bid.repository.BidRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidService Unit Tests")
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

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
    @DisplayName("submitBid — should save bid with SUBMITTED status")
    void submitBid_ShouldReturnSubmittedBid() {
        BidInput input = new BidInput(10L, "https://docs.example.com/proposal.pdf", "ENC_ABC123");
        when(bidRepository.save(any(Bid.class))).thenReturn(submittedBid);

        Bid result = bidService.submitBid(input, 2L);

        assertThat(result.getStatus()).isEqualTo(BidStatus.SUBMITTED);
        assertThat(result.getVendorId()).isEqualTo(2L);
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    @DisplayName("evaluateBid — SUBMITTED bid should be evaluated and scored")
    void evaluateBid_FromSubmitted_ShouldSetEvaluatedStatus() throws InterruptedException {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(submittedBid));

        // First save call (locking) and second (evaluated)
        Bid locked = new Bid();
        locked.setId(1L); locked.setStatus(BidStatus.LOCKED);
        locked.setTenderId(10L); locked.setVendorId(2L);
        locked.setTechnicalProposalUrl(submittedBid.getTechnicalProposalUrl());
        locked.setEncryptedFinancialProposal(submittedBid.getEncryptedFinancialProposal());

        Bid evaluated = new Bid();
        evaluated.setId(1L); evaluated.setStatus(BidStatus.EVALUATED);
        evaluated.setTechnicalScore(85.5); evaluated.setFinancialScore(90.0);

        when(bidRepository.save(any(Bid.class)))
                .thenReturn(locked)   // first save: locking
                .thenReturn(evaluated); // second save: evaluated

        Bid result = bidService.evaluateBid(1L);

        assertThat(result.getStatus()).isEqualTo(BidStatus.EVALUATED);
        assertThat(result.getTechnicalScore()).isEqualTo(85.5);
        assertThat(result.getFinancialScore()).isEqualTo(90.0);
    }

    @Test
    @DisplayName("evaluateBid — ACCEPTED bid should throw IllegalStateException")
    void evaluateBid_FromAccepted_ShouldThrowException() {
        submittedBid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(submittedBid));

        assertThatThrownBy(() -> bidService.evaluateBid(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUBMITTED");
    }

    @Test
    @DisplayName("getBidById — non-existing id should throw ResourceNotFoundException")
    void getBidById_NotFound_ShouldThrowException() {
        when(bidRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.getBidById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Bid not found");
    }
}
