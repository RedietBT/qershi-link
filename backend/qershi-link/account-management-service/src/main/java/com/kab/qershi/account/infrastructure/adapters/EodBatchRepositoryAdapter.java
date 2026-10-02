package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.EodBatchExecution;
import com.kab.qershi.account.domain.model.EodBatchStepLog;
import com.kab.qershi.account.domain.ports.outbound.EodBatchRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.EodBatchExecutionEntity;
import com.kab.qershi.account.infrastructure.persistence.EodBatchStepLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchExecutionRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataEodBatchStepLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter bridging EodBatch domain models and JPA entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class EodBatchRepositoryAdapter implements EodBatchRepositoryPort {

    private final SpringDataEodBatchExecutionRepository executionRepository;
    private final SpringDataEodBatchStepLogRepository stepLogRepository;

    public EodBatchRepositoryAdapter(SpringDataEodBatchExecutionRepository executionRepository,
                                     SpringDataEodBatchStepLogRepository stepLogRepository) {
        this.executionRepository = executionRepository;
        this.stepLogRepository = stepLogRepository;
    }

    @Override
    public EodBatchExecution saveExecution(EodBatchExecution domain) {
        EodBatchExecutionEntity entity = toEntity(domain);
        EodBatchExecutionEntity saved = executionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<EodBatchExecution> findExecutionById(UUID batchId) {
        return executionRepository.findById(batchId).map(this::toDomain);
    }

    @Override
    public Optional<EodBatchExecution> findLatestExecution() {
        return executionRepository.findTopByOrderByStartedAtDesc().map(this::toDomain);
    }

    @Override
    public List<EodBatchExecution> findAllExecutionsOrderByStartedAtDesc() {
        return executionRepository.findAllByOrderByStartedAtDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public EodBatchStepLog saveStepLog(EodBatchStepLog domain) {
        EodBatchStepLogEntity entity = new EodBatchStepLogEntity(
                domain.getBatchId(),
                domain.getStepName(),
                domain.getStatus(),
                domain.getDurationMs(),
                domain.getRecordsAffected(),
                domain.getErrorMessage()
        );
        entity.setStepId(domain.getStepId());
        entity.setCreatedAt(domain.getCreatedAt());
        EodBatchStepLogEntity saved = stepLogRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<EodBatchStepLog> findStepLogsByBatchId(UUID batchId) {
        return stepLogRepository.findByBatchIdOrderByCreatedAtAsc(batchId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private EodBatchExecution toDomain(EodBatchExecutionEntity entity) {
        if (entity == null) return null;
        return new EodBatchExecution(
                entity.getBatchId(),
                entity.getBusinessDate(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getStatus(),
                entity.getTriggeredBy(),
                entity.getTriggeredByUserId(),
                entity.getTotalAccountsAccrued(),
                entity.getTotalInterestAccrued(),
                entity.getTotalLoansEvaluated(),
                entity.getTotalAccountsDormant(),
                entity.getTotalLoansProvisioned(),
                entity.getSummaryNotes(),
                entity.getCreatedAt()
        );
    }

    private EodBatchExecutionEntity toEntity(EodBatchExecution domain) {
        if (domain == null) return null;
        EodBatchExecutionEntity entity = new EodBatchExecutionEntity();
        entity.setBatchId(domain.getBatchId());
        entity.setBusinessDate(domain.getBusinessDate());
        entity.setStartedAt(domain.getStartedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        entity.setStatus(domain.getStatus());
        entity.setTriggeredBy(domain.getTriggeredBy());
        entity.setTriggeredByUserId(domain.getTriggeredByUserId());
        entity.setTotalAccountsAccrued(domain.getTotalAccountsAccrued());
        entity.setTotalInterestAccrued(domain.getTotalInterestAccrued());
        entity.setTotalLoansEvaluated(domain.getTotalLoansEvaluated());
        entity.setTotalAccountsDormant(domain.getTotalAccountsDormant());
        entity.setTotalLoansProvisioned(domain.getTotalLoansProvisioned());
        entity.setSummaryNotes(domain.getSummaryNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private EodBatchStepLog toDomain(EodBatchStepLogEntity entity) {
        if (entity == null) return null;
        return new EodBatchStepLog(
                entity.getStepId(),
                entity.getBatchId(),
                entity.getStepName(),
                entity.getStatus(),
                entity.getDurationMs(),
                entity.getRecordsAffected(),
                entity.getErrorMessage(),
                entity.getCreatedAt()
        );
    }
}
