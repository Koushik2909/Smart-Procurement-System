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

    @Autowired
    private com.procurement.security.repository.UserRepository userRepository;

    @MutationMapping
    @PreAuthorize("hasRole('VENDOR')")
    public Bid submitBid(@Argument BidInput input) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long vendorId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return bidService.submitBid(input, vendorId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Bid> compareBids(@Argument Long tenderId) {
        return bidService.compareBids(tenderId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public List<com.procurement.bid.domain.BidEvaluation> getEvaluationResults(@Argument Long bidId) {
        return bidService.getEvaluationResults(bidId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Boolean assignEvaluator(@Argument Long bidId, @Argument Long evaluatorId) {
        return bidService.assignEvaluator(bidId, evaluatorId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public com.procurement.bid.domain.BidEvaluation evaluateTechnicalBid(@Argument Long bidId, @Argument Double score) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long evaluatorId = userRepository.findByUsername(username).orElseThrow().getId();
        return bidService.evaluateTechnicalBid(bidId, evaluatorId, score);
    }

    @MutationMapping
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public com.procurement.bid.domain.BidEvaluation evaluateFinancialBid(@Argument Long bidId, @Argument Double score) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long evaluatorId = userRepository.findByUsername(username).orElseThrow().getId();
        return bidService.evaluateFinancialBid(bidId, evaluatorId, score);
    }

    @MutationMapping
    @PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")
    public Bid finalizeEvaluation(@Argument Long bidId) {
        return bidService.finalizeEvaluation(bidId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Bid overrideEvaluation(@Argument Long bidId, @Argument Double technicalScore, @Argument Double financialScore, @Argument String reason) {
        return bidService.overrideEvaluation(bidId, technicalScore, financialScore, reason);
    }
}
