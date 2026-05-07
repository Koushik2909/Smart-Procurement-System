package com.procurement.vendor.repository;

import com.procurement.vendor.domain.VendorProfile;
import com.procurement.vendor.domain.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);
    List<VendorProfile> findByStatus(VendorStatus status);
}
