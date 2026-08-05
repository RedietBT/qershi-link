package com.kab.qershi.transaction.infrastructure.adapters;

import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTransactionRepository;
import com.kab.qershi.transaction.infrastructure.persistence.TransactionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Outbound persistence adapter implementing TransactionRepositoryPort.
 * Converts Transaction domain models to JPA entities and vice-versa.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Transaction> findByTransactionRef(String transactionRef) {
        return repository.findByTransactionRef(transactionRef).map(this::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public List<Transaction> findByAccountNo(String accountNo) {
        return repository.findByAccountNoOrderByCreatedAtDesc(accountNo)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return repository.existsByIdempotencyKey(idempotencyKey);
    }

    private TransactionEntity toEntity(Transaction domain) {
        if (domain == null) return null;
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(domain.getTransactionId());
        entity.setTransactionRef(domain.getTransactionRef());
        entity.setAccountNo(domain.getAccountNo());
        entity.setSaccoCode(domain.getSaccoCode());
        entity.setUserId(domain.getUserId());
        entity.setProcessedByUserId(domain.getProcessedByUserId());
        entity.setTransactionType(domain.getTransactionType());
        entity.setAmount(domain.getAmount());
        entity.setCurrency(domain.getCurrency());
        entity.setStatus(domain.getStatus());
        entity.setNarration(domain.getNarration());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        return entity;
    }

    private Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;
        return new Transaction(
                entity.getTransactionId(),
                entity.getTransactionRef(),
                entity.getAccountNo(),
                entity.getSaccoCode(),
                entity.getUserId(),
                entity.getProcessedByUserId(),
                entity.getTransactionType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getNarration(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt()
        );
    }
}
