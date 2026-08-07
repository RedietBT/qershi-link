package com.kab.qershi.loan.management.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST Request DTO for disbursing a loan application and activating a loan account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record DisburseLoanRequest(
        @NotNull(message = "Loan application ID is required")
        UUID applicationId,

        @NotNull(message = "Borrower user ID is required")
        UUID userId,

        @NotNull(message = "Loan product ID is required")
        UUID productId,

        @NotNull(message = "Disbursement amount is required")
        @DecimalMin(value = "1.00", message = "Disbursement amount must be at least 1.00")
        BigDecimal amount,

        @NotNull(message = "Interest rate percentage is required")
        BigDecimal interestRatePct,

        @NotNull(message = "Term duration in months is required")
        Integer termMonths,

        String repaymentFrequency,

        String interestType,

        UUID targetSavingsAccountId
) {}
