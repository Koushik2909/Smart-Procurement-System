package com.procurement.tender.graphql;

import com.procurement.tender.dto.ProcurementInsightsDTO;
import com.procurement.tender.dto.TenderAnalyticsDTO;
import com.procurement.tender.dto.VendorPerformanceDTO;
import com.procurement.tender.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @QueryMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public TenderAnalyticsDTO getTenderAnalytics() {
        return analyticsService.getTenderAnalytics();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public VendorPerformanceDTO getVendorPerformance(@Argument Long vendorId) {
        return analyticsService.getVendorPerformance(vendorId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public ProcurementInsightsDTO getProcurementInsights() {
        return analyticsService.getProcurementInsights();
    }
}
