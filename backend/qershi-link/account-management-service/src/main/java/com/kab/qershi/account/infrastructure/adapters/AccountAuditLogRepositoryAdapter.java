package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.AccountAuditLog;
import com.kab.qershi.account.domain.ports.outbound.AccountAuditLogRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.AccountAuditLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountAuditLogRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter bridging AccountAuditLog domain model and AccountAuditLogEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class AccountAuditLogRepositoryAdapter implements AccountAuditLogRepositoryPort {

    private final SpringDataAccountAuditLogRepository repository;

    public AccountAuditLogRepositoryAdapter(SpringDataAccountAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountAuditLog save(AccountAuditLog domain) {
        AccountAuditLogEntity entity = new AccountAuditLogEntity(
                domain.getLogId(),
                domain.getAccountNo(),
                domain.getUserId(),
                domain.getPerformedByUserId(),
                domain.getAction(),
                domain.getFieldName(),
                domain.getOldValue(),
                domain.getNewValue(),
                domain.getCreatedAt() != null ? domain.getCreatedAt().atOffset(ZoneOffset.UTC) : OffsetDateTime.now()
        );
        AccountAuditLogEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AccountAuditLog> findByAccountNo(String accountNo) {
        return repository.findByAccountNoOrderByCreatedAtDesc(accountNo).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountAuditLog> findByUserId(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AccountAuditLog toDomain(AccountAuditLogEntity entity) {
        if (entity == null) return null;
        return new AccountAuditLog(
                entity.getLogId(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDateTime() : null
        );
    }
}
