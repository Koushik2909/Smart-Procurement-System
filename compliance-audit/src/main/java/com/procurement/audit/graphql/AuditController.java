package com.procurement.audit.graphql;

import com.procurement.audit.domain.AuditLog;
import com.procurement.audit.service.AuditService;
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
public class AuditController {

    @Autowired
    private AuditService auditService;

    @QueryMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public List<AuditLog> getAuditLogs(@Argument String entityType, @Argument Long entityId) {
        return auditService.getAuditLogs(entityType, entityId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public List<AuditLog> getComplianceReport() {
        return auditService.getComplianceReport();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public AuditLog logAuditEvent(@Argument String eventType, @Argument String entityType,
                                   @Argument Long entityId, @Argument String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auditService.logEvent(eventType, entityType, entityId, auth.getName(), details);
    }

    @MutationMapping
    @PreAuthorize("hasRole('AUDITOR') or hasRole('ADMIN')")
    public AuditLog flagViolation(@Argument Long logId) {
        return auditService.flagViolation(logId);
    }
}
