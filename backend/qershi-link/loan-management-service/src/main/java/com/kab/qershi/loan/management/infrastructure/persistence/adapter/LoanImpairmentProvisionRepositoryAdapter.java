package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionLine;
import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionRun;
import com.kab.qershi.loan.management.domain.port.out.LoanImpairmentProvisionRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionLineEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionRunEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanImpairmentProvisionLineRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanImpairmentProvisionRunRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanImpairmentProvisionRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanImpairmentProvisionRepositoryAdapter implements LoanImpairmentProvisionRepositoryPort {

    private final SpringDataLoanImpairmentProvisionRunRepository runRepository;
    private final SpringDataLoanImpairmentProvisionLineRepository lineRepository;

    public LoanImpairmentProvisionRepositoryAdapter(
            SpringDataLoanImpairmentProvisionRunRepository runRepository,
            SpringDataLoanImpairmentProvisionLineRepository lineRepository) {
        this.runRepository = runRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    public LoanImpairmentProvisionRun saveRun(LoanImpairmentProvisionRun run) {
        LoanImpairmentProvisionRunEntity entity = toEntity(run);
        LoanImpairmentProvisionRunEntity saved = runRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LoanImpairmentProvisionRun> findRunByBusinessDate(LocalDate businessDate) {
        return runRepository.findByBusinessDate(businessDate).map(this::toDomain);
    }

    @Override
    public Optional<LoanImpairmentProvisionRun> findLatestCompletedRun() {
        return runRepository.findLatestCompleted().map(this::toDomain);
    }

    @Override
    public List<LoanImpairmentProvisionRun> findTop12CompletedRuns() {
        return runRepository.findTop12ByStatusOrderByBusinessDateDesc("COMPLETED").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanImpairmentProvisionRun> findAllRuns() {
        return runRepository.findAllOrderedByDateDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanImpairmentProvisionLine> saveAllLines(List<LoanImpairmentProvisionLine> lines) {
        List<LoanImpairmentProvisionLineEntity> entities = lines.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<LoanImpairmentProvisionLineEntity> saved = lineRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<LoanImpairmentProvisionLine> findLinesByRunId(UUID runId) {
        return lineRepository.findByRunId(runId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanImpairmentProvisionLine> findLinesByRunIdAndIfrs9Stage(UUID runId, String stage) {
        return lineRepository.findByRunIdAndIfrs9Stage(runId, stage).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countLinesByRunId(UUID runId) {
        return lineRepository.countByRunId(runId);
    }

    private LoanImpairmentProvisionRunEntity toEntity(LoanImpairmentProvisionRun domain) {
        if (domain == null) return null;
        LoanImpairmentProvisionRunEntity entity = new LoanImpairmentProvisionRunEntity();
        entity.setRunId(domain.getRunId());
        entity.setBusinessDate(domain.getBusinessDate());
        entity.setRunType(domain.getRunType());
        entity.setStatus(domain.getStatus());
        entity.setTotalLoansEvaluated(domain.getTotalLoansEvaluated() != null ? domain.getTotalLoansEvaluated() : 0);
        entity.setTotalPortfolioBalance(domain.getTotalPortfolioBalance());
        entity.setPassBalance(domain.getPassBalance());
        entity.setSpecialMentionBalance(domain.getSpecialMentionBalance());
        entity.setSubstandardBalance(domain.getSubstandardBalance());
        entity.setDoubtfulBalance(domain.getDoubtfulBalance());
        entity.setLossBalance(domain.getLossBalance());
        entity.setPassProvision(domain.getPassProvision());
        entity.setSpecialMentionProvision(domain.getSpecialMentionProvision());
        entity.setSubstandardProvision(domain.getSubstandardProvision());
        entity.setDoubtfulProvision(domain.getDoubtfulProvision());
        entity.setLossProvision(domain.getLossProvision());
        entity.setTotalProvisionRequired(domain.getTotalProvisionRequired());
        entity.setGlDebitAccount(domain.getGlDebitAccount());
        entity.setGlCreditAccount(domain.getGlCreditAccount());
        entity.setGlPostingRef(domain.getGlPostingRef());
        entity.setGlPostedAt(domain.getGlPostedAt());
        entity.setTriggeredBy(domain.getTriggeredBy());
        entity.setTriggeredByUserId(domain.getTriggeredByUserId());
        entity.setErrorMessage(domain.getErrorMessage());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCompletedAt(domain.getCompletedAt());
        return entity;
    }

    private LoanImpairmentProvisionRun toDomain(LoanImpairmentProvisionRunEntity entity) {
        if (entity == null) return null;
        return new LoanImpairmentProvisionRun(
                entity.getRunId(),
                entity.getBusinessDate(),
                entity.getRunType(),
                entity.getStatus(),
                entity.getTotalLoansEvaluated(),
                entity.getTotalPortfolioBalance(),
                entity.getPassBalance(),
                entity.getSpecialMentionBalance(),
                entity.getSubstandardBalance(),
                entity.getDoubtfulBalance(),
                entity.getLossBalance(),
                entity.getPassProvision(),
                entity.getSpecialMentionProvision(),
                entity.getSubstandardProvision(),
                entity.getDoubtfulProvision(),
                entity.getLossProvision(),
                entity.getTotalProvisionRequired(),
                entity.getGlDebitAccount(),
                entity.getGlCreditAccount(),
                entity.getGlPostingRef(),
                entity.getGlPostedAt(),
                entity.getTriggeredBy(),
                entity.getTriggeredByUserId(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }

    private LoanImpairmentProvisionLineEntity toEntity(LoanImpairmentProvisionLine domain) {
        if (domain == null) return null;
        LoanImpairmentProvisionLineEntity entity = new LoanImpairmentProvisionLineEntity();
        entity.setLineId(domain.getLineId());
        entity.setRunId(domain.getRunId());
        entity.setAccountId(domain.getAccountId());
        entity.setAccountNo(domain.getAccountNo());
        entity.setDaysPastDue(domain.getDaysPastDue());
        entity.setIfrs9Stage(domain.getIfrs9Stage());
        entity.setIfrs9BucketLabel(domain.getIfrs9BucketLabel());
        entity.setDpdFrom(domain.getDpdFrom());
        entity.setDpdTo(domain.getDpdTo());
        entity.setOutstandingPrincipal(domain.getOutstandingPrincipal());
        entity.setProvisionRatePct(domain.getProvisionRatePct());
        entity.setProvisionAmount(domain.getProvisionAmount());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private LoanImpairmentProvisionLine toDomain(LoanImpairmentProvisionLineEntity entity) {
        if (entity == null) return null;
        return new LoanImpairmentProvisionLine(
                entity.getLineId(),
                entity.getRunId(),
                entity.getAccountId(),
                entity.getAccountNo(),
                entity.getDaysPastDue(),
                entity.getIfrs9Stage(),
                entity.getIfrs9BucketLabel(),
                entity.getDpdFrom(),
                entity.getDpdTo(),
                entity.getOutstandingPrincipal(),
                entity.getProvisionRatePct(),
                entity.getProvisionAmount(),
                entity.getCreatedAt()
        );
    }
}
