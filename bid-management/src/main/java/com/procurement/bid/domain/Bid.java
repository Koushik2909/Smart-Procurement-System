package com.procurement.bid.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bids")
@Getter
@Setter
public class Bid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tender_id", nullable = false)
    private Long tenderId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status = BidStatus.CREATED;

    @Column(name = "technical_proposal_url")
    private String technicalProposalUrl;

    @Column(name = "encrypted_financial_proposal")
    private String encryptedFinancialProposal;

    @Column(name = "technical_score")
    private Double technicalScore;

    @Column(name = "financial_score")
    private Double financialScore;
    
    @Column(name = "final_score")
    private Double finalScore;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenderId() { return tenderId; }
    public void setTenderId(Long tenderId) { this.tenderId = tenderId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public BidStatus getStatus() { return status; }
    public void setStatus(BidStatus status) { this.status = status; }
    public String getTechnicalProposalUrl() { return technicalProposalUrl; }
    public void setTechnicalProposalUrl(String technicalProposalUrl) { this.technicalProposalUrl = technicalProposalUrl; }
    public String getEncryptedFinancialProposal() { return encryptedFinancialProposal; }
    public void setEncryptedFinancialProposal(String encryptedFinancialProposal) { this.encryptedFinancialProposal = encryptedFinancialProposal; }
    public Double getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }
    public Double getFinancialScore() { return financialScore; }
    public void setFinancialScore(Double financialScore) { this.financialScore = financialScore; }
    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public com.procurement.bid.domain.state.BidState toSealedState() {
        return switch (this.status) {
            case CREATED -> new com.procurement.bid.domain.state.BidState.Created();
            case SUBMITTED -> new com.procurement.bid.domain.state.BidState.Submitted();
            case LOCKED -> new com.procurement.bid.domain.state.BidState.Locked();
            case OPENED -> new com.procurement.bid.domain.state.BidState.Opened();
            case EVALUATED -> new com.procurement.bid.domain.state.BidState.Evaluated(
                this.technicalScore != null ? this.technicalScore : 0.0,
                this.financialScore != null ? this.financialScore : 0.0
            );
            case ACCEPTED -> new com.procurement.bid.domain.state.BidState.Accepted();
            case REJECTED -> new com.procurement.bid.domain.state.BidState.Rejected("Rejected during evaluation");
        };
    }
}
