package com.kab.qershi.loan.origination.infrastructure.persistence.repository;

import com.kab.qershi.loan.origination.infrastructure.persistence.entity.ApprovalWorkflowLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for approval_workflow_logs table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataApprovalWorkflowLogRepository extends JpaRepository<ApprovalWorkflowLogEntity, UUID> {

    List<ApprovalWorkflowLogEntity> findByApplicationId(UUID applicationId);
}
