package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for a Loan Account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanAccountResponse(
        UUID accountId,
        String accountNo,
        UUID applicationId,
        UUID userId,
        UUID productId,
        BigDecimal principalAmount,
        BigDecimal interestRatePct,
        Integer termMonths,
        String repaymentFrequency,
        String interestType,
        OffsetDateTime disbursementDate,
        LoanStatus status,
        OffsetDateTime createdAt
) {
    public static LoanAccountResponse fromDomain(LoanAccount domain) {
        if (domain == null) return null;
        return new LoanAccountResponse(
                domain.getAccountId(),
                domain.getAccountNo(),
                domain.getApplicationId(),
                domain.getUserId(),
                domain.getProductId(),
                domain.getPrincipalAmount(),
                domain.getInterestRatePct(),
                domain.getTermMonths(),
                domain.getRepaymentFrequency(),
                domain.getInterestType(),
                domain.getDisbursementDate(),
                domain.getStatus(),
                domain.getCreatedAt()
        );
    }
}
