package com.procurement.tender.service;

import com.procurement.core.exception.ResourceNotFoundException;
import com.procurement.tender.domain.Tender;
import com.procurement.tender.domain.TenderStatus;
import com.procurement.tender.domain.state.TenderState;
import com.procurement.tender.dto.TenderInput;
import com.procurement.tender.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TenderService {

    @Autowired
    private TenderRepository tenderRepository;

    public Tender createTender(TenderInput input, Long userId) {
        Tender tender = new Tender();
        tender.setTitle(input.title());
        tender.setDescription(input.description());
        tender.setCreatedByUserId(userId);
        if (input.submissionDeadline() != null) {
            tender.setSubmissionDeadline(LocalDateTime.parse(input.submissionDeadline(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return tenderRepository.save(tender);
    }

    public Tender getTenderById(Long id) {
        return tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + id));
    }

    public List<Tender> getActiveTenders() {
        return tenderRepository.findByStatus(TenderStatus.OPEN);
    }

    public Tender publishTender(Long id) {
        Tender tender = getTenderById(id);
        
        // Java 25 Pattern Matching for Switch on Sealed Classes
        switch (tender.toSealedState()) {
            case TenderState.Draft d -> tender.setStatus(TenderStatus.PUBLISHED);
            case TenderState.Published p -> throw new IllegalStateException("Tender is already published");
            default -> throw new IllegalStateException("Only DRAFT tenders can be published");
        }
        
        return tenderRepository.save(tender);
    }
    
    public Tender openTender(Long id) {
        Tender tender = getTenderById(id);
        
        switch (tender.toSealedState()) {
            case TenderState.Published p -> tender.setStatus(TenderStatus.OPEN);
            case TenderState.Open o -> throw new IllegalStateException("Tender is already open");
            default -> throw new IllegalStateException("Only PUBLISHED tenders can be opened");
        }
        
        return tenderRepository.save(tender);
    }

    public Tender cancelTender(Long id, String reason) {
        Tender tender = getTenderById(id);
        
        switch (tender.toSealedState()) {
            case TenderState.Closed c -> throw new IllegalStateException("Cannot cancel a closed tender");
            case TenderState.Awarded a -> throw new IllegalStateException("Cannot cancel an awarded tender");
            case TenderState.Cancelled can -> throw new IllegalStateException("Tender is already cancelled");
            default -> tender.setStatus(TenderStatus.CANCELLED);
        }
        // In a real system, we'd log the reason. For now, it's captured in the state pattern above conceptually.
        return tenderRepository.save(tender);
    }
}
