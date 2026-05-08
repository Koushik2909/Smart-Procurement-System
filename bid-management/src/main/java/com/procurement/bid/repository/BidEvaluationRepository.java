package com.procurement.bid.repository;

import com.procurement.bid.domain.BidEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidEvaluationRepository extends JpaRepository<BidEvaluation, Long> {
    List<BidEvaluation> findByBidId(Long bidId);
    Optional<BidEvaluation> findByBidIdAndEvaluatorId(Long bidId, Long evaluatorId);
}
