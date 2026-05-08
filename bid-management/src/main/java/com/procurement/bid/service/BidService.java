package com.procurement.bid.service;

import com.procurement.bid.domain.Bid;
import com.procurement.bid.domain.BidStatus;
import com.procurement.bid.domain.state.BidState;
import com.procurement.bid.dto.BidInput;
import com.procurement.bid.repository.BidRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private com.procurement.bid.repository.BidEvaluationRepository bidEvaluationRepository;

    @Autowired
    private com.procurement.security.repository.FraudBlocklistRepository fraudBlocklistRepository;

    public Bid submitBid(BidInput input, Long vendorId) {
        fraudBlocklistRepository.findByUserId(vendorId).ifPresent(blocklist -> {
            if (blocklist.getStatus() == com.procurement.security.domain.BlocklistStatus.BLOCKED) {
                throw new RuntimeException("Access Denied: Vendor is blocklisted for: " + blocklist.getReason());
            }
        });

        Bid bid = new Bid();
        bid.setTenderId(input.tenderId());
        bid.setVendorId(vendorId);
        bid.setTechnicalProposalUrl(input.technicalProposalUrl());
        bid.setEncryptedFinancialProposal(input.encryptedFinancialProposal());
        bid.setStatus(BidStatus.SUBMITTED);
        return bidRepository.save(bid);
    }

    public Bid getBidById(Long id) {
        return bidRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));
    }

    public List<Bid> getBidsByTender(Long tenderId) {
        return bidRepository.findByTenderId(tenderId);
    }

    public List<Bid> compareBids(Long tenderId) {
        return bidRepository.findByTenderId(tenderId).stream()
                .filter(b -> b.getFinalScore() != null)
                .sorted((b1, b2) -> Double.compare(b2.getFinalScore(), b1.getFinalScore()))
                .toList();
    }

    public boolean assignEvaluator(Long bidId, Long evaluatorId) {
        Bid bid = getBidById(bidId);
        if (bid.getVendorId().equals(evaluatorId)) {
            throw new RuntimeException("Conflict of Interest: Vendor cannot evaluate their own bid");
        }
        com.procurement.bid.domain.BidEvaluation eval = new com.procurement.bid.domain.BidEvaluation();
        eval.setBidId(bidId);
        eval.setEvaluatorId(evaluatorId);
        bidEvaluationRepository.save(eval);
        return true;
    }

    public com.procurement.bid.domain.BidEvaluation evaluateTechnicalBid(Long bidId, Long evaluatorId, Double score) {
        com.procurement.bid.domain.BidEvaluation eval = bidEvaluationRepository.findByBidIdAndEvaluatorId(bidId, evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not assigned to this bid"));
        eval.setTechnicalScore(score);
        return bidEvaluationRepository.save(eval);
    }

    public com.procurement.bid.domain.BidEvaluation evaluateFinancialBid(Long bidId, Long evaluatorId, Double score) {
        com.procurement.bid.domain.BidEvaluation eval = bidEvaluationRepository.findByBidIdAndEvaluatorId(bidId, evaluatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator not assigned to this bid"));
        eval.setFinancialScore(score);
        return bidEvaluationRepository.save(eval);
    }

    public Bid finalizeEvaluation(Long bidId) {
        Bid bid = getBidById(bidId);
        List<com.procurement.bid.domain.BidEvaluation> evaluations = bidEvaluationRepository.findByBidId(bidId);
        
        if (evaluations.isEmpty()) {
            throw new RuntimeException("No evaluations found for this bid");
        }

        double avgTech = evaluations.stream().mapToDouble(e -> e.getTechnicalScore() != null ? e.getTechnicalScore() : 0).average().orElse(0);
        double avgFin = evaluations.stream().mapToDouble(e -> e.getFinancialScore() != null ? e.getFinancialScore() : 0).average().orElse(0);

        // Assume Tender weights w1=0.7, w2=0.3 for cross-module simplicity
        double w1 = 0.7;
        double w2 = 0.3;
        double finalScore = (w1 * avgTech) + (w2 * avgFin);

        bid.setTechnicalScore(avgTech);
        bid.setFinancialScore(avgFin);
        bid.setFinalScore(finalScore);
        bid.setStatus(BidStatus.EVALUATED);
        return bidRepository.save(bid);
    }

    public Bid overrideEvaluation(Long bidId, Double technicalScore, Double financialScore, String reason) {
        Bid bid = getBidById(bidId);
        double w1 = 0.7;
        double w2 = 0.3;
        double finalScore = (w1 * technicalScore) + (w2 * financialScore);
        
        bid.setTechnicalScore(technicalScore);
        bid.setFinancialScore(financialScore);
        bid.setFinalScore(finalScore);
        bid.setStatus(BidStatus.EVALUATED);
        // We could optionally log the 'reason' string to an Audit log here
        return bidRepository.save(bid);
    }

    public List<com.procurement.bid.domain.BidEvaluation> getEvaluationResults(Long bidId) {
        return bidEvaluationRepository.findByBidId(bidId);
    }
}
