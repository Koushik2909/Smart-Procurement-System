package com.procurement.auction.repository;

import com.procurement.auction.domain.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    List<AuctionBid> findByAuctionIdOrderByBidAmountAscBidTimeAsc(Long auctionId);
}
