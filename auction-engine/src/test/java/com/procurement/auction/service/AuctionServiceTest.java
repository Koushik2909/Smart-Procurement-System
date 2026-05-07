package com.procurement.auction.service;

import com.procurement.auction.domain.Auction;
import com.procurement.auction.domain.AuctionBid;
import com.procurement.auction.domain.AuctionStatus;
import com.procurement.auction.dto.AuctionInput;
import com.procurement.auction.repository.AuctionBidRepository;
import com.procurement.auction.repository.AuctionRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService Unit Tests")
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AuctionBidRepository auctionBidRepository;

    @InjectMocks
    private AuctionService auctionService;

    private Auction activeAuction;

    @BeforeEach
    void setUp() {
        activeAuction = new Auction();
        activeAuction.setId(1L);
        activeAuction.setTenderId(10L);
        activeAuction.setStatus(AuctionStatus.ACTIVE);
        activeAuction.setStartingPrice(1_000_000.0);
        activeAuction.setMinimumDecrement(5_000.0);
        activeAuction.setStartTime(LocalDateTime.now().minusMinutes(5));
        // End time far in the future so virtual thread monitor won't close it during test
        activeAuction.setEndTime(LocalDateTime.now().plusHours(2));
    }

    @Test
    @DisplayName("placeBid — valid bid meeting minimum decrement should be accepted")
    void placeBid_ValidDecrement_ShouldSaveBid() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));

        // No existing bids — first bid
        when(auctionBidRepository.findByAuctionIdOrderByBidAmountAscBidTimeAsc(1L)).thenReturn(List.of());

        AuctionBid savedBid = new AuctionBid();
        savedBid.setId(1L);
        savedBid.setAuctionId(1L);
        savedBid.setVendorId(2L);
        savedBid.setBidAmount(900_000.0);
        savedBid.setBidTime(LocalDateTime.now());
        when(auctionBidRepository.save(any(AuctionBid.class))).thenReturn(savedBid);

        AuctionBid result = auctionService.placeBid(1L, 2L, 900_000.0);

        assertThat(result.getBidAmount()).isEqualTo(900_000.0);
        assertThat(result.getVendorId()).isEqualTo(2L);
        verify(auctionBidRepository).save(any(AuctionBid.class));
    }

    @Test
    @DisplayName("placeBid — bid not meeting minimum decrement should throw IllegalArgumentException")
    void placeBid_InsufficientDecrement_ShouldThrowException() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));

        AuctionBid existingLowest = new AuctionBid();
        existingLowest.setBidAmount(900_000.0);
        when(auctionBidRepository.findByAuctionIdOrderByBidAmountAscBidTimeAsc(1L))
                .thenReturn(List.of(existingLowest));

        // 899_000 is only 1000 below 900_000 — but minimum decrement is 5_000
        assertThatThrownBy(() -> auctionService.placeBid(1L, 3L, 899_000.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least");
    }

    @Test
    @DisplayName("placeBid — on CLOSED auction should throw IllegalStateException")
    void placeBid_ClosedAuction_ShouldThrowException() {
        activeAuction.setStatus(AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));

        assertThatThrownBy(() -> auctionService.placeBid(1L, 2L, 850_000.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("closeAuction — should set status to CLOSED")
    void closeAuction_ShouldReturnClosedAuction() {
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));
        Auction closed = new Auction();
        closed.setId(1L);
        closed.setStatus(AuctionStatus.CLOSED);
        when(auctionRepository.save(any(Auction.class))).thenReturn(closed);

        Auction result = auctionService.closeAuction(1L);

        assertThat(result.getStatus()).isEqualTo(AuctionStatus.CLOSED);
    }

    @Test
    @DisplayName("getAuctionById — non-existing id should throw ResourceNotFoundException")
    void getAuctionById_NotFound_ShouldThrowException() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.getAuctionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Auction not found");
    }
}
