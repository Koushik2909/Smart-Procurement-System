package com.procurement.vendor.graphql;

import com.procurement.vendor.domain.VendorProfile;
import com.procurement.vendor.dto.VendorInput;
import com.procurement.vendor.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public VendorProfile getVendorById(@Argument Long id) {
        return vendorService.getVendorById(id);
    }

    @QueryMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public List<VendorProfile> getQualifiedVendors() {
        return vendorService.getQualifiedVendors();
    }

    @QueryMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public List<VendorProfile> getPendingVendors() {
        return vendorService.getPendingVendors();
    }

    @Autowired
    private com.procurement.security.repository.UserRepository userRepository;

    @MutationMapping
    @PreAuthorize("hasRole('VENDOR')")
    public VendorProfile registerVendorProfile(@Argument VendorInput input) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return vendorService.registerVendorProfile(userId, input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public VendorProfile approveVendor(@Argument Long id) {
        return vendorService.approveVendor(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public VendorProfile rejectVendor(@Argument Long id) {
        return vendorService.rejectVendor(id);
    }
}
