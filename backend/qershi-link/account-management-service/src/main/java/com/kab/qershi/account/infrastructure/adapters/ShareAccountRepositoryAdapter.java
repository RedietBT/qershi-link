package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.ShareAccount;
import com.kab.qershi.account.domain.ports.outbound.ShareAccountRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.ShareAccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataShareAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence Adapter bridging ShareAccount domain model and JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ShareAccountRepositoryAdapter implements ShareAccountRepositoryPort {

    private final SpringDataShareAccountRepository repository;

    public ShareAccountRepositoryAdapter(SpringDataShareAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public ShareAccount save(ShareAccount domain) {
        ShareAccountEntity entity = toEntity(domain);
        ShareAccountEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ShareAccount> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ShareAccount> findByMemberId(UUID memberId) {
        return repository.findByMemberId(memberId).map(this::toDomain);
    }

    @Override
    public Optional<ShareAccount> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    @Override
    public List<ShareAccount> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return repository.existsByMemberId(memberId);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ShareAccount toDomain(ShareAccountEntity entity) {
        if (entity == null) return null;
        return new ShareAccount(
                entity.getId(),
                entity.getMemberId(),
                entity.getAccountNumber(),
                entity.getTotalShares(),
                entity.getShareNominalValue(),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getSaccoCode(),
                entity.getBranchCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ShareAccountEntity toEntity(ShareAccount domain) {
        if (domain == null) return null;
        ShareAccountEntity entity = new ShareAccountEntity();
        entity.setId(domain.getId());
        entity.setMemberId(domain.getMemberId());
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setTotalShares(domain.getTotalShares());
        entity.setShareNominalValue(domain.getShareNominalValue());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setStatus(domain.getStatus());
        entity.setSaccoCode(domain.getSaccoCode());
        entity.setBranchCode(domain.getBranchCode());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
