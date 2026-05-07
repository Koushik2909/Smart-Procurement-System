package com.procurement.bid.graphql;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.dto.BidInput;
import com.procurement.bid.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BidController {

    @Autowired
    private BidService bidService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Bid getBidById(@Argument Long id) {
        return bidService.getBidById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Bid> getBidsByTender(@Argument Long tenderId) {
        return bidService.getBidsByTender(tenderId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('VENDOR')")
    public Bid submitBid(@Argument BidInput input) {
        Long vendorId = 2L; // Mock ID. Real system would extract from JWT
        return bidService.submitBid(input, vendorId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public Bid evaluateBid(@Argument Long id) throws InterruptedException {
        return bidService.evaluateBid(id);
    }
}
