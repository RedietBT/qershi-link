package com.kab.qershi.loan.origination.infrastructure.rest.dto;

import com.kab.qershi.loan.origination.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response DTO for loan applications.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanApplicationResponse(
        UUID applicationId,
        String applicationNo,
        UUID userId,
        UUID groupId,
        UUID productId,
        String scoringType,
        BigDecimal amountRequested,
        BigDecimal amountApproved,
        ApplicationStatus status,
        CreditScoring creditScoring,
        List<Collateral> collaterals,
        List<ApprovalLog> approvalLogs,
        Instant createdAt,
        Instant updatedAt
) {
    public static LoanApplicationResponse fromDomain(LoanApplication domain) {
        return new LoanApplicationResponse(
                domain.getApplicationId(),
                domain.getApplicationNo(),
                domain.getUserId(),
                domain.getGroupId(),
                domain.getProductId(),
                domain.getScoringType(),
                domain.getAmountRequested(),
                domain.getAmountApproved(),
                domain.getStatus(),
                domain.getCreditScoring(),
                domain.getCollaterals(),
                domain.getApprovalLogs(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
