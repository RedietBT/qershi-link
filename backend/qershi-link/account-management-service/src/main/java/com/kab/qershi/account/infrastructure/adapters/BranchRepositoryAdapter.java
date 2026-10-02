package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.Branch;
import com.kab.qershi.account.domain.ports.outbound.BranchRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.BranchEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataBranchRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter bridging Branch domain model and BranchEntity JPA entity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class BranchRepositoryAdapter implements BranchRepositoryPort {

    private final SpringDataBranchRepository repository;

    public BranchRepositoryAdapter(SpringDataBranchRepository repository) {
        this.repository = repository;
    }

    @Override
    public Branch save(Branch domain) {
        BranchEntity entity = toEntity(domain);
        BranchEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Branch> findById(UUID branchId) {
        return repository.findById(branchId).map(this::toDomain);
    }

    @Override
    public Optional<Branch> findByBranchCode(String branchCode) {
        return repository.findByBranchCode(branchCode).map(this::toDomain);
    }

    @Override
    public List<Branch> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Branch> findByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByBranchCode(String branchCode) {
        return repository.existsByBranchCode(branchCode);
    }

    private Branch toDomain(BranchEntity entity) {
        if (entity == null) return null;
        return new Branch(
                entity.getBranchId(),
                entity.getBranchCode(),
                entity.getBranchName(),
                entity.getRegion(),
                entity.getAddress(),
                entity.getContactPhone(),
                entity.getManagerUserId(),
                entity.getVaultGlCode(),
                entity.getDiscretionaryLendingLimit(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private BranchEntity toEntity(Branch domain) {
        if (domain == null) return null;
        BranchEntity entity = new BranchEntity(
                domain.getBranchId(),
                domain.getBranchCode(),
                domain.getBranchName(),
                domain.getRegion(),
                domain.getAddress(),
                domain.getContactPhone(),
                domain.getManagerUserId(),
                domain.getVaultGlCode(),
                domain.getDiscretionaryLendingLimit(),
                domain.getStatus()
        );
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
