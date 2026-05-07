package com.procurement.tender.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenders")
@Getter
@Setter
public class Tender extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenderStatus status = TenderStatus.DRAFT;

    @Column(name = "submission_deadline")
    private LocalDateTime submissionDeadline;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;
    
    // Convert DB Status to Java 25 Sealed Class state for pattern matching
    public com.procurement.tender.domain.state.TenderState toSealedState() {
        return switch (this.status) {
            case DRAFT -> new com.procurement.tender.domain.state.TenderState.Draft();
            case PUBLISHED -> new com.procurement.tender.domain.state.TenderState.Published();
            case OPEN -> new com.procurement.tender.domain.state.TenderState.Open();
            case CLOSED -> new com.procurement.tender.domain.state.TenderState.Closed();
            case EVALUATION -> new com.procurement.tender.domain.state.TenderState.Evaluation();
            case AWARDED -> new com.procurement.tender.domain.state.TenderState.Awarded();
            case CANCELLED -> new com.procurement.tender.domain.state.TenderState.Cancelled("Cancelled by user");
        };
    }
}
