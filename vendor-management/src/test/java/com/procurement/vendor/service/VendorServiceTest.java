package com.procurement.vendor.service;

import com.procurement.vendor.domain.VendorProfile;
import com.procurement.vendor.domain.VendorStatus;
import com.procurement.vendor.dto.VendorInput;
import com.procurement.vendor.repository.VendorProfileRepository;
import com.procurement.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorService Unit Tests")
class VendorServiceTest {

    @Mock
    private VendorProfileRepository vendorProfileRepository;

    @InjectMocks
    private VendorService vendorService;

    private VendorProfile pendingVendor;

    @BeforeEach
    void setUp() {
        pendingVendor = new VendorProfile();
        pendingVendor.setId(1L);
        pendingVendor.setUserId(2L);
        pendingVendor.setCompanyName("Tech Corp");
        pendingVendor.setTaxId("TX12345");
        pendingVendor.setStatus(VendorStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("registerVendorProfile — should save vendor with PENDING_APPROVAL status")
    void registerVendorProfile_ShouldReturnPendingVendor() {
        VendorInput input = new VendorInput("Tech Corp", "TX12345", "http://kyc.doc");
        when(vendorProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(vendorProfileRepository.save(any(VendorProfile.class))).thenReturn(pendingVendor);

        VendorProfile result = vendorService.registerVendorProfile(input, 2L);

        assertThat(result.getStatus()).isEqualTo(VendorStatus.PENDING_APPROVAL);
        assertThat(result.getUserId()).isEqualTo(2L);
        verify(vendorProfileRepository).save(any(VendorProfile.class));
    }

    @Test
    @DisplayName("registerVendorProfile — should throw if vendor already exists")
    void registerVendorProfile_AlreadyExists_ShouldThrowException() {
        VendorInput input = new VendorInput("Tech Corp", "TX12345", "http://kyc.doc");
        when(vendorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(pendingVendor));

        assertThatThrownBy(() -> vendorService.registerVendorProfile(input, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User already has a vendor profile");
    }

    @Test
    @DisplayName("approveVendor — should change status to APPROVED")
    void approveVendor_ShouldSetStatusApproved() {
        when(vendorProfileRepository.findById(1L)).thenReturn(Optional.of(pendingVendor));
        when(vendorProfileRepository.save(any(VendorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorProfile result = vendorService.approveVendor(1L);

        assertThat(result.getStatus()).isEqualTo(VendorStatus.APPROVED);
    }

    @Test
    @DisplayName("rejectVendor — should change status to REJECTED")
    void rejectVendor_ShouldSetStatusRejected() {
        when(vendorProfileRepository.findById(1L)).thenReturn(Optional.of(pendingVendor));
        when(vendorProfileRepository.save(any(VendorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorProfile result = vendorService.rejectVendor(1L);

        assertThat(result.getStatus()).isEqualTo(VendorStatus.REJECTED);
    }

    @Test
    @DisplayName("getVendorById — non-existing id should throw exception")
    void getVendorById_NotFound_ShouldThrowException() {
        when(vendorProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vendorService.getVendorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vendor profile not found");
    }
}
