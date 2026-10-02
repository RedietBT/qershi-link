package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanDelinquencySnapshot;
import com.kab.qershi.loan.management.domain.port.out.LoanDelinquencyRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanDelinquencySnapshotEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanDelinquencySnapshotRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanDelinquencyRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanDelinquencyRepositoryAdapter implements LoanDelinquencyRepositoryPort {

    private final SpringDataLoanDelinquencySnapshotRepository repository;

    public LoanDelinquencyRepositoryAdapter(SpringDataLoanDelinquencySnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LoanDelinquencySnapshot> saveAll(List<LoanDelinquencySnapshot> snapshots) {
        List<LoanDelinquencySnapshotEntity> entities = snapshots.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<LoanDelinquencySnapshotEntity> saved = repository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<LoanDelinquencySnapshot> findByBusinessDate(LocalDate businessDate) {
        return repository.findByBusinessDate(businessDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LoanDelinquencySnapshot> findByAccountIdAndBusinessDate(UUID accountId, LocalDate businessDate) {
        return repository.findByAccountIdAndBusinessDate(accountId, businessDate).map(this::toDomain);
    }

    @Override
    public List<LoanDelinquencySnapshot> findLatestSnapshots() {
        return repository.findLatestSnapshots().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDelinquencySnapshot> findLatestDelinquentSnapshots() {
        return repository.findLatestDelinquentSnapshots().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LoanDelinquencySnapshotEntity toEntity(LoanDelinquencySnapshot domain) {
        if (domain == null) return null;
        LoanDelinquencySnapshotEntity entity = new LoanDelinquencySnapshotEntity();
        entity.setSnapshotId(domain.getSnapshotId());
        entity.setAccountId(domain.getAccountId());
        entity.setBusinessDate(domain.getBusinessDate());
        entity.setDaysPastDue(domain.getDaysPastDue());
        entity.setOverduePrincipal(domain.getOverduePrincipal());
        entity.setOverdueInterest(domain.getOverdueInterest());
        entity.setTotalOverdue(domain.getTotalOverdue());
        entity.setParBucket(domain.getParBucket());
        entity.setProvisionRatePct(domain.getProvisionRatePct());
        entity.setProvisionAmount(domain.getProvisionAmount());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private LoanDelinquencySnapshot toDomain(LoanDelinquencySnapshotEntity entity) {
        if (entity == null) return null;
        return new LoanDelinquencySnapshot(
                entity.getSnapshotId(),
                entity.getAccountId(),
                entity.getBusinessDate(),
                entity.getDaysPastDue(),
                entity.getOverduePrincipal(),
                entity.getOverdueInterest(),
                entity.getTotalOverdue(),
                entity.getParBucket(),
                entity.getProvisionRatePct(),
                entity.getProvisionAmount(),
                entity.getCreatedAt()
        );
    }
}
