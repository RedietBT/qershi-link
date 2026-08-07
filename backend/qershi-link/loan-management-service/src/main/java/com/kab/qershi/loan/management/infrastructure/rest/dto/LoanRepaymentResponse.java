package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for a Loan Repayment Audit Transaction.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanRepaymentResponse(
        UUID repaymentId,
        UUID accountId,
        String transactionRef,
        BigDecimal amountPaid,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal penaltyPortion,
        OffsetDateTime paymentDate,
        String paymentChannel,
        String remarks
) {
    public static LoanRepaymentResponse fromDomain(LoanRepayment domain) {
        if (domain == null) return null;
        return new LoanRepaymentResponse(
                domain.getRepaymentId(),
                domain.getAccountId(),
                domain.getTransactionRef(),
                domain.getAmountPaid(),
                domain.getPrincipalPortion(),
                domain.getInterestPortion(),
                domain.getPenaltyPortion(),
                domain.getPaymentDate(),
                domain.getPaymentChannel(),
                domain.getRemarks()
        );
    }
}
