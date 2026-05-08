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

    @Autowired
    private com.procurement.security.repository.UserRepository userRepository;

    @MutationMapping
    @PreAuthorize("hasRole('PROCUREMENT_OFFICER') or hasRole('ADMIN')")
    public Tender createTender(@Argument TenderInput input) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
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
