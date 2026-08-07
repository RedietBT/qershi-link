package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanAccountRepositoryPort via JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanAccountRepositoryAdapter implements LoanAccountRepositoryPort {

    private final SpringDataLoanAccountRepository repository;

    public LoanAccountRepositoryAdapter(SpringDataLoanAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanAccount save(LoanAccount account) {
        LoanAccountEntity entity = toEntity(account);
        LoanAccountEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LoanAccount> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<LoanAccount> findByApplicationId(UUID applicationId) {
        return repository.findByApplicationId(applicationId).map(this::toDomain);
    }

    @Override
    public List<LoanAccount> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LoanAccountEntity toEntity(LoanAccount domain) {
        if (domain == null) return null;
        LoanAccountEntity entity = new LoanAccountEntity();
        entity.setAccountId(domain.getAccountId());
        entity.setAccountNo(domain.getAccountNo());
        entity.setApplicationId(domain.getApplicationId());
        entity.setUserId(domain.getUserId());
        entity.setProductId(domain.getProductId());
        entity.setPrincipalAmount(domain.getPrincipalAmount());
        entity.setInterestRatePct(domain.getInterestRatePct());
        entity.setTermMonths(domain.getTermMonths());
        entity.setRepaymentFrequency(domain.getRepaymentFrequency());
        entity.setInterestType(domain.getInterestType());
        entity.setDisbursementDate(domain.getDisbursementDate());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private LoanAccount toDomain(LoanAccountEntity entity) {
        if (entity == null) return null;
        return new LoanAccount(
                entity.getAccountId(),
                entity.getAccountNo(),
                entity.getApplicationId(),
                entity.getUserId(),
                entity.getProductId(),
                entity.getPrincipalAmount(),
                entity.getInterestRatePct(),
                entity.getTermMonths(),
                entity.getRepaymentFrequency(),
                entity.getInterestType(),
                entity.getDisbursementDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
