package com.procurement.vendor.service;

import com.procurement.core.exception.ResourceNotFoundException;
import com.procurement.vendor.domain.VendorProfile;
import com.procurement.vendor.domain.VendorStatus;
import com.procurement.vendor.dto.VendorInput;
import com.procurement.vendor.repository.VendorProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    @Autowired
    private VendorProfileRepository vendorProfileRepository;

    public VendorProfile registerVendorProfile(Long userId, VendorInput input) {
        if (vendorProfileRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has a vendor profile");
        }

        VendorProfile profile = new VendorProfile();
        profile.setUserId(userId);
        profile.setCompanyName(input.companyName());
        profile.setTaxId(input.taxId());
        profile.setKycDocumentUrl(input.kycDocumentUrl());
        return vendorProfileRepository.save(profile);
    }

    public VendorProfile getVendorById(Long id) {
        return vendorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor Profile not found"));
    }

    public List<VendorProfile> getQualifiedVendors() {
        return vendorProfileRepository.findByStatus(VendorStatus.APPROVED);
    }

    public List<VendorProfile> getPendingVendors() {
        return vendorProfileRepository.findByStatus(VendorStatus.PENDING_APPROVAL);
    }

    public VendorProfile approveVendor(Long id) {
        VendorProfile profile = getVendorById(id);
        profile.setStatus(VendorStatus.APPROVED);
        return vendorProfileRepository.save(profile);
    }

    public VendorProfile rejectVendor(Long id) {
        VendorProfile profile = getVendorById(id);
        profile.setStatus(VendorStatus.REJECTED);
        return vendorProfileRepository.save(profile);
    }
}
