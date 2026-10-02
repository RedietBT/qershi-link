package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.ChartOfAccount;
import com.kab.qershi.account.domain.model.GlAccountType;
import com.kab.qershi.account.domain.ports.outbound.ChartOfAccountRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.ChartOfAccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataChartOfAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging ChartOfAccount domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ChartOfAccountRepositoryAdapter implements ChartOfAccountRepositoryPort {

    private final SpringDataChartOfAccountRepository repository;

    public ChartOfAccountRepositoryAdapter(SpringDataChartOfAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChartOfAccount save(ChartOfAccount domain) {
        ChartOfAccountEntity entity = toEntity(domain);
        ChartOfAccountEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ChartOfAccount> findById(UUID accountId) {
        return repository.findById(accountId).map(this::toDomain);
    }

    @Override
    public Optional<ChartOfAccount> findByGlCode(String glCode) {
        return repository.findByGlCode(glCode).map(this::toDomain);
    }

    @Override
    public List<ChartOfAccount> findByParentGlCode(String parentGlCode) {
        return repository.findByParentGlCode(parentGlCode).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartOfAccount> findAllOrderByGlCodeAsc() {
        return repository.findAllByOrderByGlCodeAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartOfAccount> findByAccountTypeOrderByGlCodeAsc(GlAccountType accountType) {
        return repository.findByAccountTypeOrderByGlCodeAsc(accountType).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByGlCode(String glCode) {
        return repository.existsByGlCode(glCode);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ChartOfAccount toDomain(ChartOfAccountEntity entity) {
        if (entity == null) return null;
        return new ChartOfAccount(
                entity.getAccountId(),
                entity.getGlCode(),
                entity.getAccountName(),
                entity.getAccountType(),
                entity.getParentGlCode(),
                entity.getCurrency(),
                entity.getBalance(),
                entity.getIsReconciled(),
                entity.getAllowManualJournal(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ChartOfAccountEntity toEntity(ChartOfAccount domain) {
        if (domain == null) return null;
        ChartOfAccountEntity entity = new ChartOfAccountEntity();
        entity.setAccountId(domain.getAccountId());
        entity.setGlCode(domain.getGlCode());
        entity.setAccountName(domain.getAccountName());
        entity.setAccountType(domain.getAccountType());
        entity.setParentGlCode(domain.getParentGlCode());
        entity.setCurrency(domain.getCurrency());
        entity.setBalance(domain.getBalance());
        entity.setIsReconciled(domain.getIsReconciled());
        entity.setAllowManualJournal(domain.getAllowManualJournal());
        entity.setStatus(domain.getStatus());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
