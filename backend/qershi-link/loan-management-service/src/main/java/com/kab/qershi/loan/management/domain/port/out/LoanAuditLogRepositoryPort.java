package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanAuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Repository Port for LoanAuditLog persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanAuditLogRepositoryPort {

    LoanAuditLog save(LoanAuditLog log);

    List<LoanAuditLog> findByAccountNoOrderByCreatedAtDesc(String accountNo);

    List<LoanAuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LoanAuditLog> findAllPaginated(int page, int size);
}
