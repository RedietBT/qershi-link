package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request body for opening a Fixed Term Deposit contract.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record OpenTermDepositRequest(

        @NotBlank(message = "Source savings account number is required")
        String accountNo,

        @NotNull(message = "Principal amount is required")
        @DecimalMin(value = "100.00", message = "Minimum FD principal is 100.00 ETB")
        BigDecimal principalAmount,

        @NotNull(message = "Tenor months is required")
        @Min(value = 1, message = "Minimum tenor is 1 month")
        @Max(value = 120, message = "Maximum tenor is 120 months (10 years)")
        Integer tenorMonths,

        @NotNull(message = "Agreed interest rate is required")
        @DecimalMin(value = "0.01", message = "Interest rate must be positive")
        @DecimalMax(value = "50.00", message = "Interest rate cannot exceed 50%")
        BigDecimal agreedInterestRatePa,

        @DecimalMin(value = "0.00", message = "Penalty rate cannot be negative")
        @DecimalMax(value = "20.00", message = "Penalty rate cannot exceed 20%")
        BigDecimal earlyBreakPenaltyPct,

        boolean autoRollover,

        Integer rolloverTenorMonths,

        String makerNotes
) {}
