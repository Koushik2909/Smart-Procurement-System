package com.procurement.tender.graphql;

import com.procurement.tender.domain.Tender;
import com.procurement.tender.dto.TenderInput;
import com.procurement.tender.service.TenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TenderController {

    @Autowired
    private TenderService tenderService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Tender getTenderById(@Argument Long id) {
        return tenderService.getTenderById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Tender> getActiveTenders() {
        return tenderService.getActiveTenders();
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Tender createTender(@Argument TenderInput input) {
        // For simplicity, we assign a dummy user ID here or fetch from a custom UserDetails
        // In a complete system, the auth token would have the internal user ID.
        Long userId = 1L; // Mock ID since JWT doesn't currently embed internal ID
        return tenderService.createTender(input, userId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Tender publishTender(@Argument Long id) {
        return tenderService.publishTender(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Tender openTender(@Argument Long id) {
        return tenderService.openTender(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Tender cancelTender(@Argument Long id, @Argument String reason) {
        return tenderService.cancelTender(id, reason);
    }
}
