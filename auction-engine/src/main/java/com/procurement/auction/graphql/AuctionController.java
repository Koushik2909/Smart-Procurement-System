package com.procurement.auction.graphql;

import com.procurement.auction.domain.Auction;
import com.procurement.auction.domain.AuctionBid;
import com.procurement.auction.dto.AuctionInput;
import com.procurement.auction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Auction getAuctionDetails(@Argument Long id) {
        return auctionService.getAuctionById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<AuctionBid> getAuctionLeaderboard(@Argument Long auctionId) {
        return auctionService.getLeaderboard(auctionId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Auction startAuction(@Argument AuctionInput input) {
        return auctionService.startAuction(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('VENDOR')")
    public AuctionBid placeBidInAuction(@Argument Long auctionId, @Argument Double amount) {
        Long vendorId = 2L; // Mock; real system resolves from JWT
        return auctionService.placeBid(auctionId, vendorId, amount);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Auction closeAuction(@Argument Long id) {
        return auctionService.closeAuction(id);
    }
}
