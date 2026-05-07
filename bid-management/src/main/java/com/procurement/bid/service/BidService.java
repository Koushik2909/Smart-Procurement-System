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

    public Bid submitBid(BidInput input, Long vendorId) {
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

    // Example of Virtual Threads & Structured Concurrency for Evaluation Workflows
    public Bid evaluateBid(Long bidId) throws InterruptedException {
        Bid bid = getBidById(bidId);
        
        switch (bid.toSealedState()) {
            case BidState.Submitted s -> {
                // We can evaluate. Change to Locked for evaluation.
                bid.setStatus(BidStatus.LOCKED);
                bidRepository.save(bid);
            }
            case BidState.Locked l -> { /* Already locked, proceed */ }
            default -> throw new IllegalStateException("Bid must be in SUBMITTED state to evaluate");
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Concurrent evaluation tasks using Virtual Threads
            java.util.concurrent.Future<Double> techEval = executor.submit(() -> performTechnicalEvaluation(bid.getTechnicalProposalUrl()));
            java.util.concurrent.Future<Double> finEval = executor.submit(() -> performFinancialEvaluation(bid.getEncryptedFinancialProposal()));
            java.util.concurrent.Future<Boolean> compliance = executor.submit(() -> checkVendorCompliance(bid.getVendorId()));

            if (!compliance.get()) {
                bid.setStatus(BidStatus.REJECTED);
                return bidRepository.save(bid);
            }

            bid.setTechnicalScore(techEval.get());
            bid.setFinancialScore(finEval.get());
            bid.setStatus(BidStatus.EVALUATED);
            return bidRepository.save(bid);

        } catch (Exception e) {
            throw new RuntimeException("Bid evaluation failed: " + e.getMessage(), e);
        }
    }

    // Mock Evaluation Tasks
    private Double performTechnicalEvaluation(String url) throws InterruptedException {
        Thread.sleep(100); // Simulate network call or processing
        return 85.5; // Dummy score
    }

    private Double performFinancialEvaluation(String encryptedProposal) throws InterruptedException {
        Thread.sleep(150); // Simulate decryption and analysis
        return 90.0; // Dummy score
    }

    private Boolean checkVendorCompliance(Long vendorId) throws InterruptedException {
        Thread.sleep(50); // Simulate DB check
        return true; // Compliant
    }
}
