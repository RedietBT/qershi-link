package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.AuditLog;
import com.kab.qershi.auth.domain.ports.inbound.SystemAuditUseCase;
import com.kab.qershi.auth.domain.ports.outbound.AuditLogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service managing global system security and administrative audit logs.
 * Asynchronously records authentication events, password rotations, and SACCO onboardings.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Service
public class SystemAuditService implements SystemAuditUseCase {

    private static final Logger log = LoggerFactory.getLogger(SystemAuditService.class);
    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public SystemAuditService(AuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuditLog(UUID userId, UUID saccoId, String action, String resourceAffected,
                               String status, String ipAddress, String details) {
        try {
            AuditLog logEntry = new AuditLog(
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
            auditLogRepositoryPort.save(logEntry);
            log.debug("System Audit Log persisted: action={}, userId={}, saccoId={}, status={}", action, userId, saccoId, status);
        } catch (Exception ex) {
            log.error("Failed to persist System Audit Log for action {}: {}", action, ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(int page, int size) {
        return auditLogRepositoryPort.findAll(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getTenantAuditLogs(UUID tenantSaccoId) {
        if (tenantSaccoId == null) {
            throw new IllegalArgumentException("Tenant SACCO ID cannot be null.");
        }
        return auditLogRepositoryPort.findBySaccoId(tenantSaccoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsBySacco(UUID saccoId) {
        if (saccoId == null) {
            throw new IllegalArgumentException("SACCO ID cannot be null.");
        }
        return auditLogRepositoryPort.findBySaccoId(saccoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByUser(UUID userId, boolean isSuperAdmin, UUID tenantSaccoId) {
        List<AuditLog> userLogs = auditLogRepositoryPort.findByUserId(userId);
        if (!isSuperAdmin && tenantSaccoId != null) {
            userLogs = userLogs.stream()
                    .filter(l -> l.getSaccoId() != null && l.getSaccoId().equals(tenantSaccoId))
                    .toList();
        }
        return userLogs;
    }
}
