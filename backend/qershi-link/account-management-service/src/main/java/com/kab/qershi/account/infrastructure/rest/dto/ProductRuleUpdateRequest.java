package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for updating per-product risk limits and Maker-Checker settings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ProductRuleUpdateRequest(
    @NotNull(message = "minOperatingBalance is required")
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal minOperatingBalance,

    @NotNull(message = "maxBalanceLimit is required")
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal maxBalanceLimit,

    @NotNull(message = "singleWithdrawalLimit is required")
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal singleWithdrawalLimit,

    @NotNull(message = "dailyWithdrawalLimit is required")
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal dailyWithdrawalLimit,

    Boolean enableMakerChecker
) {}
