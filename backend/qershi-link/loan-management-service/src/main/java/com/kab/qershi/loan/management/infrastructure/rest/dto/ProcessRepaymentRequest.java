package com.kab.qershi.loan.management.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST Request DTO for processing a loan repayment transaction.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ProcessRepaymentRequest(
        @NotNull(message = "Loan account ID is required")
        UUID accountId,

        @NotNull(message = "Repayment amount is required")
        @DecimalMin(value = "1.00", message = "Repayment amount must be at least 1.00")
        BigDecimal amountPaid,

        String paymentChannel,

        String remarks
) {}
