package com.kab.qershi.loan.origination.infrastructure.persistence.adapter;

import com.kab.qershi.loan.origination.domain.model.ApprovalLog;
import com.kab.qershi.loan.origination.domain.model.WorkflowAction;
import com.kab.qershi.loan.origination.domain.ports.outbound.ApprovalLogRepositoryPort;
import com.kab.qershi.loan.origination.infrastructure.persistence.entity.ApprovalWorkflowLogEntity;
import com.kab.qershi.loan.origination.infrastructure.persistence.repository.SpringDataApprovalWorkflowLogRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound JPA repository adapter implementing ApprovalLogRepositoryPort.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class ApprovalLogRepositoryAdapter implements ApprovalLogRepositoryPort {

    private final SpringDataApprovalWorkflowLogRepository repository;

    public ApprovalLogRepositoryAdapter(SpringDataApprovalWorkflowLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApprovalLog save(ApprovalLog log) {
        ApprovalWorkflowLogEntity entity = new ApprovalWorkflowLogEntity(
                log.getLogId(),
                log.getApplicationId(),
                log.getActionBy(),
                log.getActionType().name(),
                log.getRemarks(),
                log.getActionAt()
        );
        ApprovalWorkflowLogEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ApprovalLog> findByApplicationId(UUID applicationId) {
        return repository.findByApplicationId(applicationId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ApprovalLog toDomain(ApprovalWorkflowLogEntity entity) {
        return new ApprovalLog(
                entity.getLogId(),
                entity.getApplicationId(),
                entity.getActionBy(),
                WorkflowAction.valueOf(entity.getActionType()),
                entity.getRemarks(),
                entity.getActionAt()
        );
    }
}
