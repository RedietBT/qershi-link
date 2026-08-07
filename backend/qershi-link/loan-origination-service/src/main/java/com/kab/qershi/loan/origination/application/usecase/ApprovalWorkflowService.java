package com.kab.qershi.loan.origination.application.usecase;

import com.kab.qershi.loan.origination.domain.model.ApplicationStatus;
import com.kab.qershi.loan.origination.domain.model.ApprovalLog;
import com.kab.qershi.loan.origination.domain.model.LoanApplication;
import com.kab.qershi.loan.origination.domain.model.WorkflowAction;
import com.kab.qershi.loan.origination.domain.ports.inbound.ApprovalWorkflowUseCase;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanApplicationRepositoryPort;
import com.kab.qershi.loan.origination.infrastructure.adapters.NotificationGrpcClientAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service implementing ApprovalWorkflowUseCase.
 * Enforces dual-control Maker-Checker governance protocol preventing self-approval.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class ApprovalWorkflowService implements ApprovalWorkflowUseCase {

    private static final Logger log = LoggerFactory.getLogger(ApprovalWorkflowService.class);

    private final LoanApplicationRepositoryPort applicationRepositoryPort;
    private final NotificationGrpcClientAdapter notificationGrpcClientAdapter;
    private final JdbcTemplate jdbcTemplate;

    public ApprovalWorkflowService(LoanApplicationRepositoryPort applicationRepositoryPort,
                                  NotificationGrpcClientAdapter notificationGrpcClientAdapter,
                                  JdbcTemplate jdbcTemplate) {
        this.applicationRepositoryPort = applicationRepositoryPort;
        this.notificationGrpcClientAdapter = notificationGrpcClientAdapter;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public LoanApplication processApproval(ProcessApprovalCommand command) {
        if (command.applicationId() == null) {
            throw new IllegalArgumentException("Application ID cannot be null.");
        }
        if (command.actionByUserId() == null) {
            throw new IllegalArgumentException("Action by user ID cannot be null.");
        }
        if (command.actionType() == null) {
            throw new IllegalArgumentException("Workflow action type cannot be null.");
        }

        LoanApplication application = applicationRepositoryPort.findById(command.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Loan application not found: " + command.applicationId()));

        // 🛡️ Maker-Checker Identity Guard: Prevent self-approval (Maker == Checker)
        if (command.actionByUserId().equals(application.getUserId())) {
            log.warn("Maker-Checker Violation Attempt: User {} tried to approve their own loan application {}",
                    command.actionByUserId(), application.getApplicationNo());
            throw new SecurityException("Maker-Checker Governance Violation: A loan application cannot be reviewed or approved by the applicant themselves.");
        }

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Loan application " + application.getApplicationNo() +
                    " is not under review. Current status: " + application.getStatus());
        }

        ApprovalLog actionLog = new ApprovalLog(
                UUID.randomUUID(),
                application.getApplicationId(),
                command.actionByUserId(),
                command.actionType(),
                command.remarks(),
                Instant.now()
        );
        application.addApprovalLog(actionLog);

        if (command.actionType() == WorkflowAction.APPROVE) {
            BigDecimal approvedAmount = command.amountApproved() != null && command.amountApproved().compareTo(BigDecimal.ZERO) > 0
                    ? command.amountApproved()
                    : application.getAmountRequested();

            application.setAmountApproved(approvedAmount);
            application.setStatus(ApplicationStatus.APPROVED);
            log.info("Loan Application {} APPROVED for {} ETB by checker user {}.",
                    application.getApplicationNo(), approvedAmount, command.actionByUserId());

            // 📡 Dispatch gRPC SMS notification for LOAN_APPLICATION_APPROVED
            dispatchApprovalNotification(application);

        } else if (command.actionType() == WorkflowAction.REJECT) {
            application.setStatus(ApplicationStatus.REJECTED);
            log.info("Loan Application {} REJECTED by checker user {}.",
                    application.getApplicationNo(), command.actionByUserId());
        }

        return applicationRepositoryPort.save(application);
    }

    private void dispatchApprovalNotification(LoanApplication application) {
        try {
            String recipientPhone = null;
            String memberName = "Member";

            try {
                String sql = "SELECT primary_phone, full_name FROM master_schema.users WHERE user_id = ?";
                Map<String, Object> userMap = jdbcTemplate.queryForMap(sql, application.getUserId());
                if (userMap.get("primary_phone") != null) {
                    recipientPhone = userMap.get("primary_phone").toString();
                }
                if (userMap.get("full_name") != null) {
                    memberName = userMap.get("full_name").toString();
                }
            } catch (Exception ex) {
                log.trace("Could not resolve phone number for user {}: {}", application.getUserId(), ex.getMessage());
            }

            if (recipientPhone != null && !recipientPhone.isBlank()) {
                Map<String, String> params = new HashMap<>();
                params.put("memberName", memberName);
                params.put("appNo", application.getApplicationNo());
                params.put("amount", application.getAmountApproved().toString());

                notificationGrpcClientAdapter.sendNotification(recipientPhone, "LOAN_APPLICATION_APPROVED", params);
            }
        } catch (Exception ex) {
            log.error("Failed to dispatch gRPC approval notification for application {}: {}",
                    application.getApplicationNo(), ex.getMessage(), ex);
        }
    }
}
