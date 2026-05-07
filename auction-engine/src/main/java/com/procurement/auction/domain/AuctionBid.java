package com.procurement.auction.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auction_bids")
@Getter
@Setter
public class AuctionBid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "bid_amount", nullable = false)
    private Double bidAmount;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;
}
