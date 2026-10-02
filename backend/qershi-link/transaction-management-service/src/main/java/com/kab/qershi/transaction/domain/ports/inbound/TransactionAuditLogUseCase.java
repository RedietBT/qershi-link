package com.kab.qershi.transaction.domain.ports.inbound;

import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import java.util.List;

/**
 * Inbound port exposing financial transaction audit inquiry use cases.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransactionAuditLogUseCase {

    List<TransactionAuditLog> getAuditLogs(int page, int size);

    List<TransactionAuditLog> getAuditLogsByTransactionRef(String transactionRef);

    List<TransactionAuditLog> getAuditLogsByAccountNo(String accountNo);
}
