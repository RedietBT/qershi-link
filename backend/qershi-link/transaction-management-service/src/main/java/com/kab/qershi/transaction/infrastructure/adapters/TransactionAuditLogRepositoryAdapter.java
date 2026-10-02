package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionAuditLogRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTransactionAuditLogRepository;
import com.kab.qershi.transaction.infrastructure.persistence.TransactionAuditLogEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbound repository adapter for TransactionAuditLog domain model.
 * Bridges TransactionAuditLogRepositoryPort with SpringDataTransactionAuditLogRepository.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TransactionAuditLogRepositoryAdapter implements TransactionAuditLogRepositoryPort {

    private final SpringDataTransactionAuditLogRepository repository;

    public TransactionAuditLogRepositoryAdapter(SpringDataTransactionAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransactionAuditLog save(TransactionAuditLog log) {
        if (log == null) return null;
        TransactionAuditLogEntity entity = toEntity(log);
        TransactionAuditLogEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<TransactionAuditLog> findAll(int page, int size) {
        int boundedSize = Math.max(1, Math.min(size, 200));
        int boundedPage = Math.max(0, page);
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(boundedPage, boundedSize))
                .map(this::toDomain)
                .getContent();
    }

    @Override
    public List<TransactionAuditLog> findByTransactionRef(String transactionRef) {
        return repository.findByTransactionRefOrderByCreatedAtDesc(transactionRef).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<TransactionAuditLog> findByAccountNo(String accountNo) {
        return repository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionAuditLog toDomain(TransactionAuditLogEntity entity) {
        if (entity == null) return null;
        return new TransactionAuditLog(
                entity.getLogId(),
                entity.getTransactionRef(),
                entity.getAccountNo(),
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getDetails(),
                entity.getCreatedAt()
        );
    }

    private TransactionAuditLogEntity toEntity(TransactionAuditLog domain) {
        if (domain == null) return null;
        return new TransactionAuditLogEntity(
                domain.getLogId(),
                domain.getTransactionRef(),
                domain.getAccountNo(),
                domain.getPerformedByUserId(),
                domain.getAction(),
                domain.getDetails(),
                domain.getCreatedAt()
        );
    }
}
