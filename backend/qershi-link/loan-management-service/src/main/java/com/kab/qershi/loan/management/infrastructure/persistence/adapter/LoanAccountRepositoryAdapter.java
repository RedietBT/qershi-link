package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanStatus;
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
    public List<LoanAccount> saveAll(List<LoanAccount> accounts) {
        List<LoanAccountEntity> entities = accounts.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<LoanAccountEntity> saved = repository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<LoanAccount> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<LoanAccount> findByAccountNo(String accountNo) {
        return repository.findByAccountNo(accountNo).map(this::toDomain);
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

    @Override
    public List<LoanAccount> findByStatus(LoanStatus status) {
        return repository.findByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAccount> findByStatusIn(List<LoanStatus> statuses) {
        return repository.findByStatusIn(statuses).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanAccount> findAll() {
        return repository.findAll().stream()
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
        entity.setDaysPastDue(domain.getDaysPastDue() != null ? domain.getDaysPastDue() : 0);
        entity.setParBucket(domain.getParBucket() != null ? domain.getParBucket() : "CURRENT");
        entity.setProvisionRatePct(domain.getProvisionRatePct());
        entity.setProvisionAmount(domain.getProvisionAmount());
        entity.setLastParEvaluationDate(domain.getLastParEvaluationDate());
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
                entity.getDaysPastDue(),
                entity.getParBucket(),
                entity.getProvisionRatePct(),
                entity.getProvisionAmount(),
                entity.getLastParEvaluationDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
