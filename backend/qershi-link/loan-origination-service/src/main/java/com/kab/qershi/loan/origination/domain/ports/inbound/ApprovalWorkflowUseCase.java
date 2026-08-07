package com.kab.qershi.loan.origination.domain.ports.inbound;

import com.kab.qershi.loan.origination.domain.model.LoanApplication;
import com.kab.qershi.loan.origination.domain.model.WorkflowAction;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound port enforcing Maker-Checker dual control loan approval decisions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ApprovalWorkflowUseCase {

    record ProcessApprovalCommand(
            UUID applicationId,
            UUID actionByUserId,
            WorkflowAction actionType,
            BigDecimal amountApproved,
            String remarks
    ) {}

    LoanApplication processApproval(ProcessApprovalCommand command);
}
