package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new per-product / account type risk rule.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ProductRuleCreateRequest(
    @NotBlank(message = "productCode is required")
    String productCode,

    @NotBlank(message = "productName is required")
    String productName,

    String category,

    @NotNull(message = "minOperatingBalance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "minOperatingBalance must be positive or zero")
    BigDecimal minOperatingBalance,

    @NotNull(message = "maxBalanceLimit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "maxBalanceLimit must be positive or zero")
    BigDecimal maxBalanceLimit,

    @NotNull(message = "singleWithdrawalLimit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "singleWithdrawalLimit must be positive or zero")
    BigDecimal singleWithdrawalLimit,

    @NotNull(message = "dailyWithdrawalLimit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "dailyWithdrawalLimit must be positive or zero")
    BigDecimal dailyWithdrawalLimit,

    Boolean enableMakerChecker
) {}
