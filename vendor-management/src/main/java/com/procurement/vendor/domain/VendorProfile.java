package com.procurement.vendor.domain;

import com.procurement.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vendor_profiles")
@Getter
@Setter
public class VendorProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "kyc_document_url")
    private String kycDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.PENDING_APPROVAL;
}
