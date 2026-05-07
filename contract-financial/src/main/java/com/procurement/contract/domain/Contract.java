package com.procurement.contract.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tender_id", nullable = false)
    private Long tenderId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "contract_value", nullable = false)
    private Double contractValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "digital_signature")
    private String digitalSignature;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "terminated_reason")
    private String terminatedReason;
}
