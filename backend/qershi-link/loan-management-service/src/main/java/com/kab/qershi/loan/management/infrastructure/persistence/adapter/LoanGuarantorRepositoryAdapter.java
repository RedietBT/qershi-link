package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanAccountGuarantor;
import com.kab.qershi.loan.management.domain.port.out.LoanGuarantorRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountGuarantorEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountGuarantorRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanGuarantorRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanGuarantorRepositoryAdapter implements LoanGuarantorRepositoryPort {

    private final SpringDataLoanAccountGuarantorRepository repository;

    public LoanGuarantorRepositoryAdapter(SpringDataLoanAccountGuarantorRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanAccountGuarantor save(LoanAccountGuarantor domain) {
        LoanAccountGuarantorEntity entity = toEntity(domain);
        LoanAccountGuarantorEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<LoanAccountGuarantor> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAccountGuarantor> findByAccountIdAndStatus(UUID accountId, String status) {
        return repository.findByAccountIdAndStatus(accountId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAccountGuarantor> findByApplicationId(UUID applicationId) {
        return repository.findByApplicationId(applicationId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LoanAccountGuarantorEntity toEntity(LoanAccountGuarantor domain) {
        if (domain == null) return null;
        return new LoanAccountGuarantorEntity(
                domain.getGuarantorId(),
                domain.getAccountId(),
                domain.getApplicationId(),
                domain.getGuarantorUserId(),
                domain.getGuarantorName(),
                domain.getGuarantorPhone(),
                domain.getSavingsAccountNo(),
                domain.getGuaranteedAmount(),
                domain.getLienId(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private LoanAccountGuarantor toDomain(LoanAccountGuarantorEntity entity) {
        if (entity == null) return null;
        return new LoanAccountGuarantor(
                entity.getGuarantorId(),
                entity.getAccountId(),
                entity.getApplicationId(),
                entity.getGuarantorUserId(),
                entity.getGuarantorName(),
                entity.getGuarantorPhone(),
                entity.getSavingsAccountNo(),
                entity.getGuaranteedAmount(),
                entity.getLienId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
