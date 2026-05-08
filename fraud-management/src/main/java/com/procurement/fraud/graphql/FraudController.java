package com.procurement.fraud.graphql;

import com.procurement.fraud.service.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FraudController {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @QueryMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public List<String> detectBidCollusion(@Argument Long tenderId) {
        return fraudDetectionService.detectBidCollusion(tenderId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public List<String> analyzeVendorPatterns() {
        return fraudDetectionService.analyzeVendorPatterns();
    }

    @QueryMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public List<String> getFraudAlerts(@Argument Long tenderId) {
        return fraudDetectionService.getFraudAlerts(tenderId);
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public com.procurement.security.domain.FraudBlocklist blockUser(@Argument Long userId, @Argument String reason) {
        return fraudDetectionService.blockUser(userId, reason);
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public com.procurement.security.domain.FraudBlocklist unblockUser(@Argument Long userId) {
        return fraudDetectionService.unblockUser(userId);
    }
}
