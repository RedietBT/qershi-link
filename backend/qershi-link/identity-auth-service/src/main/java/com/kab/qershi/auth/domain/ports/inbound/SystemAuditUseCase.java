package com.kab.qershi.auth.domain.ports.inbound;

import com.kab.qershi.auth.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for recording security audit events and inspecting compliance logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SystemAuditUseCase {

    void recordAuditLog(UUID userId, UUID saccoId, String action, String resourceAffected,
                        String status, String ipAddress, String details);

    List<AuditLog> getAuditLogs(int page, int size);

    List<AuditLog> getTenantAuditLogs(UUID tenantSaccoId);

    List<AuditLog> getAuditLogsBySacco(UUID saccoId);

    List<AuditLog> getAuditLogsByUser(UUID userId, boolean isSuperAdmin, UUID tenantSaccoId);
}
