package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.model.LoanAuditLog;
import com.kab.qershi.loan.management.domain.port.in.LoanAuditLogUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAuditLogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic service managing Loan Lifecycle Audit Log queries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class LoanAuditLogService implements LoanAuditLogUseCase {

    private final LoanAuditLogRepositoryPort auditLogRepository;

    public LoanAuditLogService(LoanAuditLogRepositoryPort auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<LoanAuditLog> getAuditLogs(int page, int size) {
        return auditLogRepository.findAllPaginated(page, size);
    }

    @Override
    public List<LoanAuditLog> getAuditLogsByAccountNo(String accountNo) {
        return auditLogRepository.findByAccountNoOrderByCreatedAtDesc(accountNo);
    }

    @Override
    public List<LoanAuditLog> getAuditLogsByUserId(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
