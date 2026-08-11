package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.infrastructure.persistence.AuditLogEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for logging global system security and administrative actions in master_schema.audit_logs.
 * Asynchronously records authentication events, password rotations, and SACCO onboardings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class SystemAuditService {

    private static final Logger log = LoggerFactory.getLogger(SystemAuditService.class);
    private final SpringDataAuditLogRepository auditLogRepository;

    public SystemAuditService(SpringDataAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records a global system audit log entry in a separate transaction propagation level (REQUIRES_NEW)
     * so audit trails are preserved even if the outer business operation throws an exception.
     *
     * @param userId            UUID of the user (optional).
     * @param saccoId           UUID of the SACCO (optional).
     * @param action            Name of the security action (e.g. LOGIN_SUCCESS, LOGIN_FAILED).
     * @param resourceAffected Resource affected (e.g. USER, SACCO, PIN).
     * @param status            Execution status (e.g. SUCCESS, FAILURE, BLOCKED).
     * @param ipAddress         Client IP address.
     * @param details           Detailed context or error message.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuditLog(UUID userId, UUID saccoId, String action, String resourceAffected,
                               String status, String ipAddress, String details) {
        try {
            AuditLogEntity logEntry = new AuditLogEntity(
                    null,
                    userId,
                    saccoId,
                    action,
                    resourceAffected,
                    status != null ? status : "SUCCESS",
                    ipAddress,
                    details,
                    OffsetDateTime.now()
            );
            auditLogRepository.save(logEntry);
            log.debug("System Audit Log persisted: action={}, userId={}, saccoId={}, status={}", action, userId, saccoId, status);
        } catch (Exception ex) {
            log.error("Failed to persist System Audit Log for action {}: {}", action, ex.getMessage(), ex);
        }
    }
}
