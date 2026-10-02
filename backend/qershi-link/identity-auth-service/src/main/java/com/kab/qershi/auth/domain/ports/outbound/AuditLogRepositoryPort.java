package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for persisting and querying platform security and administrative audit logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findAll(int page, int size);

    List<AuditLog> findBySaccoId(UUID saccoId);

    List<AuditLog> findByUserId(UUID userId);
}
