package com.kab.qershi.loan.management.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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

        String remarks,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Member phone number must be a valid phone number")
        String memberPhone
) {}
