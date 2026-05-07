package com.procurement.contract.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "milestone_name")
    private String milestoneName;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.SCHEDULED;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "penalty_amount")
    private Double penaltyAmount;
}
