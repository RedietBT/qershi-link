package com.kab.qershi.loan.origination.infrastructure.rest.dto;

import com.kab.qershi.loan.origination.domain.model.WorkflowAction;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * REST request DTO for Maker-Checker loan approval decisions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ApprovalRequest(
        @NotNull(message = "Workflow action type (APPROVE/REJECT) is required")
        WorkflowAction actionType,

        BigDecimal amountApproved,

        String remarks
) {}
