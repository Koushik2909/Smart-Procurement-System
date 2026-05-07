package com.procurement.audit.service;

import com.procurement.audit.domain.AuditLog;
import com.procurement.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog logEvent(String eventType, String entityType, Long entityId,
                              String performedBy, String details) {
        AuditLog log = new AuditLog();
        log.setEventType(eventType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPerformedBy(performedBy);
        log.setDetails(details);
        return auditLogRepository.save(log);
    }

    public AuditLog flagViolation(Long logId) {
        AuditLog log = auditLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
        log.setIsViolation(true);
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getComplianceReport() {
        return auditLogRepository.findByIsViolationTrue();
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
