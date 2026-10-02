package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanAuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Port for retrieving loan audit log timelines.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanAuditLogUseCase {

    List<LoanAuditLog> getAuditLogs(int page, int size);

    List<LoanAuditLog> getAuditLogsByAccountNo(String accountNo);

    List<LoanAuditLog> getAuditLogsByUserId(UUID userId);
}
