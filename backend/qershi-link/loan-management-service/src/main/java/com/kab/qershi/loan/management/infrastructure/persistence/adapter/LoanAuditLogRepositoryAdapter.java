package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanAuditLog;
import com.kab.qershi.loan.management.domain.port.out.LoanAuditLogRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAuditLogEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanAuditLogRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanAuditLogRepositoryAdapter implements LoanAuditLogRepositoryPort {

    private final SpringDataLoanAuditLogRepository repository;

    public LoanAuditLogRepositoryAdapter(SpringDataLoanAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanAuditLog save(LoanAuditLog domain) {
        LoanAuditLogEntity entity = toEntity(domain);
        LoanAuditLogEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<LoanAuditLog> findByAccountNoOrderByCreatedAtDesc(String accountNo) {
        return repository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAuditLog> findAllPaginated(int page, int size) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 200))).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LoanAuditLogEntity toEntity(LoanAuditLog domain) {
        if (domain == null) return null;
        return new LoanAuditLogEntity(
                domain.getLogId(),
                domain.getAccountNo(),
                domain.getUserId(),
                domain.getPerformedByUserId(),
                domain.getAction(),
                domain.getFieldName(),
                domain.getOldValue(),
                domain.getNewValue(),
                domain.getCreatedAt()
        );
    }

    private LoanAuditLog toDomain(LoanAuditLogEntity entity) {
        if (entity == null) return null;
        return new LoanAuditLog(
                entity.getLogId(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getCreatedAt()
        );
    }
}
