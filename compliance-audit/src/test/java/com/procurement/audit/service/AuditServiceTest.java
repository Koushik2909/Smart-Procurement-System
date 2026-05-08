package com.procurement.audit.service;

import com.procurement.audit.domain.AuditLog;
import com.procurement.audit.repository.AuditLogRepository;
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
@DisplayName("AuditService Unit Tests")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new AuditLog();
        sampleLog.setId(1L);
        sampleLog.setEventType("TENDER_CREATED");
        sampleLog.setEntityType("Tender");
        sampleLog.setEntityId(10L);
        sampleLog.setPerformedBy("admin1");
        sampleLog.setIsViolation(false);
    }

    @Test
    @DisplayName("logEvent — should save new AuditLog")
    void logEvent_ShouldSaveAndReturnLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog result = auditService.logEvent("TEST_EVENT", "TestEntity", 99L, "user2", "details");

        assertThat(result.getEventType()).isEqualTo("TEST_EVENT");
        assertThat(result.getEntityType()).isEqualTo("TestEntity");
        assertThat(result.getIsViolation()).isFalse();
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("flagViolation — should set isViolation to true")
    void flagViolation_ShouldSetViolationFlag() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(sampleLog));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog result = auditService.flagViolation(1L);

        assertThat(result.getIsViolation()).isTrue();
    }

    @Test
    @DisplayName("flagViolation — non-existent log should throw Exception")
    void flagViolation_NotFound_ShouldThrowException() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditService.flagViolation(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Audit log not found");
    }
}
