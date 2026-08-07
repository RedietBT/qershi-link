package com.kab.qershi.loan.origination.infrastructure.persistence.adapter;

import com.kab.qershi.loan.origination.domain.model.*;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanApplicationRepositoryPort;
import com.kab.qershi.loan.origination.infrastructure.persistence.entity.*;
import com.kab.qershi.loan.origination.infrastructure.persistence.repository.SpringDataLoanApplicationRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound JPA repository adapter implementing LoanApplicationRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepositoryPort {

    private final SpringDataLoanApplicationRepository repository;

    public LoanApplicationRepositoryAdapter(SpringDataLoanApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanApplication save(LoanApplication domain) {
        LoanApplicationEntity entity = toEntity(domain);
        LoanApplicationEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LoanApplication> findById(UUID applicationId) {
        return repository.findById(applicationId).map(this::toDomain);
    }

    @Override
    public Optional<LoanApplication> findByApplicationNo(String applicationNo) {
        return repository.findByApplicationNo(applicationNo).map(this::toDomain);
    }

    @Override
    public List<LoanApplication> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countApplicationsCreatedInCurrentYear() {
        return repository.count();
    }

    private LoanApplicationEntity toEntity(LoanApplication domain) {
        LoanCreditScoringEntity scoringEntity = domain.getCreditScoring() != null ? new LoanCreditScoringEntity(
                domain.getCreditScoring().getScoringId(),
                domain.getApplicationId(),
                domain.getCreditScoring().getSavingsConsistency(),
                domain.getCreditScoring().getHistoricalYield(),
                domain.getCreditScoring().getProjectedYield(),
                domain.getCreditScoring().getLandSizeHectares(),
                domain.getCreditScoring().getCalculatedScore(),
                domain.getCreditScoring().isPassedEligibility(),
                domain.getCreditScoring().getCreatedAt()
        ) : null;

        List<LoanCollateralEntity> collateralEntities = domain.getCollaterals().stream()
                .map(c -> new LoanCollateralEntity(
                        c.getCollateralId(),
                        domain.getApplicationId(),
                        c.getType(),
                        c.getEstimatedValue(),
                        c.getDocumentUrl(),
                        c.getCreatedAt()
                )).collect(Collectors.toList());

        List<ApprovalWorkflowLogEntity> logEntities = domain.getApprovalLogs().stream()
                .map(l -> new ApprovalWorkflowLogEntity(
                        l.getLogId(),
                        domain.getApplicationId(),
                        l.getActionBy(),
                        l.getActionType().name(),
                        l.getRemarks(),
                        l.getActionAt()
                )).collect(Collectors.toList());

        return new LoanApplicationEntity(
                domain.getApplicationId(),
                domain.getApplicationNo(),
                domain.getUserId(),
                domain.getGroupId(),
                domain.getProductId(),
                domain.getScoringType(),
                domain.getAmountRequested(),
                domain.getAmountApproved(),
                domain.getStatus().name(),
                scoringEntity,
                collateralEntities,
                logEntities,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private LoanApplication toDomain(LoanApplicationEntity entity) {
        CreditScoring scoring = entity.getCreditScoring() != null ? new CreditScoring(
                entity.getCreditScoring().getScoringId(),
                entity.getApplicationId(),
                entity.getCreditScoring().getSavingsConsistency(),
                entity.getCreditScoring().getHistoricalYield(),
                entity.getCreditScoring().getProjectedYield(),
                entity.getCreditScoring().getLandSizeHectares(),
                entity.getCreditScoring().getCalculatedScore(),
                entity.getCreditScoring().isPassedEligibility(),
                entity.getCreditScoring().getCreatedAt()
        ) : null;

        List<Collateral> collaterals = entity.getCollaterals().stream()
                .map(c -> new Collateral(
                        c.getCollateralId(),
                        entity.getApplicationId(),
                        c.getType(),
                        c.getEstimatedValue(),
                        c.getDocumentUrl(),
                        c.getCreatedAt()
                )).collect(Collectors.toList());

        List<ApprovalLog> logs = entity.getApprovalLogs().stream()
                .map(l -> new ApprovalLog(
                        l.getLogId(),
                        entity.getApplicationId(),
                        l.getActionBy(),
                        WorkflowAction.valueOf(l.getActionType()),
                        l.getRemarks(),
                        l.getActionAt()
                )).collect(Collectors.toList());

        return new LoanApplication(
                entity.getApplicationId(),
                entity.getApplicationNo(),
                entity.getUserId(),
                entity.getGroupId(),
                entity.getProductId(),
                entity.getScoringType(),
                entity.getAmountRequested(),
                entity.getAmountApproved(),
                ApplicationStatus.valueOf(entity.getStatus()),
                scoring,
                collaterals,
                logs,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
