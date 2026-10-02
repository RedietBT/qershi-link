package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.AccountAuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for persisting and querying account lifecycle audit logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountAuditLogRepositoryPort {

    AccountAuditLog save(AccountAuditLog log);

    List<AccountAuditLog> findByAccountNo(String accountNo);

    List<AccountAuditLog> findByUserId(UUID userId);
}
