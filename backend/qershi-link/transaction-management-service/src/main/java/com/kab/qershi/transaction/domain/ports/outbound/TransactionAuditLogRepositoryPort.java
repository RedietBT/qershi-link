package com.kab.qershi.transaction.domain.ports.outbound;

import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import java.util.List;

/**
 * Outbound port for persisting and querying transaction financial audit log entries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransactionAuditLogRepositoryPort {

    TransactionAuditLog save(TransactionAuditLog log);

    List<TransactionAuditLog> findAll(int page, int size);

    List<TransactionAuditLog> findByTransactionRef(String transactionRef);

    List<TransactionAuditLog> findByAccountNo(String accountNo);
}
