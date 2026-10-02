package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import com.kab.qershi.transaction.domain.ports.inbound.TransactionAuditLogUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionAuditLogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service implementing TransactionAuditLogUseCase.
 * Depends solely on domain model and outbound repository port.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class TransactionAuditLogService implements TransactionAuditLogUseCase {

    private final TransactionAuditLogRepositoryPort auditLogRepositoryPort;

    public TransactionAuditLogService(TransactionAuditLogRepositoryPort auditLogRepositoryPort) {
        this.auditLogRepositoryPort = auditLogRepositoryPort;
    }

    @Override
    public List<TransactionAuditLog> getAuditLogs(int page, int size) {
        return auditLogRepositoryPort.findAll(page, size);
    }

    @Override
    public List<TransactionAuditLog> getAuditLogsByTransactionRef(String transactionRef) {
        if (transactionRef == null || transactionRef.isBlank()) {
            throw new IllegalArgumentException("Transaction reference must not be blank.");
        }
        return auditLogRepositoryPort.findByTransactionRef(transactionRef.trim());
    }

    @Override
    public List<TransactionAuditLog> getAuditLogsByAccountNo(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank.");
        }
        return auditLogRepositoryPort.findByAccountNo(accountNo.trim());
    }
}
