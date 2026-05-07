package com.procurement.auction.service;

import com.procurement.auction.domain.Auction;
import com.procurement.auction.domain.AuctionBid;
import com.procurement.auction.domain.AuctionStatus;
import com.procurement.auction.dto.AuctionInput;
import com.procurement.auction.repository.AuctionBidRepository;
import com.procurement.auction.repository.AuctionRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionBidRepository auctionBidRepository;

    // Virtual Thread executor for concurrent auction processing
    private final ExecutorService virtualThreadExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    public Auction startAuction(AuctionInput input) {
        Auction auction = new Auction();
        auction.setTenderId(input.tenderId());
        auction.setStartingPrice(input.startingPrice());
        auction.setMinimumDecrement(input.minimumDecrement());
        auction.setStartTime(LocalDateTime.parse(input.startTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        auction.setEndTime(LocalDateTime.parse(input.endTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        auction.setStatus(AuctionStatus.ACTIVE);
        Auction saved = auctionRepository.save(auction);

        // Spin up a virtual thread to monitor auction end time
        virtualThreadExecutor.submit(() -> monitorAuctionDeadline(saved.getId()));

        return saved;
    }

    public AuctionBid placeBid(Long auctionId, Long vendorId, Double amount) {
        Auction auction = getAuctionById(auctionId);

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new IllegalStateException("Auction deadline has passed");
        }

        // Get current lowest bid
        List<AuctionBid> bids = getLeaderboard(auctionId);
        if (!bids.isEmpty()) {
            Double currentLowest = bids.getFirst().getBidAmount();
            if (amount >= currentLowest - auction.getMinimumDecrement()) {
                throw new IllegalArgumentException(
                    "Bid must be at least " + auction.getMinimumDecrement() + " lower than current lowest: " + currentLowest);
            }
        }

        AuctionBid bid = new AuctionBid();
        bid.setAuctionId(auctionId);
        bid.setVendorId(vendorId);
        bid.setBidAmount(amount);
        bid.setBidTime(LocalDateTime.now());
        return auctionBidRepository.save(bid);
    }

    public Auction closeAuction(Long id) {
        Auction auction = getAuctionById(id);
        auction.setStatus(AuctionStatus.CLOSED);
        return auctionRepository.save(auction);
    }

    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found"));
    }

    public List<AuctionBid> getLeaderboard(Long auctionId) {
        return auctionBidRepository.findByAuctionIdOrderByBidAmountAscBidTimeAsc(auctionId);
    }

    // Runs on a Virtual Thread to monitor and auto-close auction
    private void monitorAuctionDeadline(Long auctionId) {
        try {
            Auction auction = getAuctionById(auctionId);
            long waitMs = java.time.Duration.between(LocalDateTime.now(), auction.getEndTime()).toMillis();
            if (waitMs > 0) {
                Thread.sleep(waitMs);
            }
            closeAuction(auctionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
