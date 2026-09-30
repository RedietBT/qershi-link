package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for updating SACCO Maker-Checker & Policy Rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record MakerCheckerRuleRequest(
    Boolean enableMemberOnboardingChecker,
    Boolean enableAccountOpeningChecker,
    Boolean enableAccountFreezeChecker,
    Boolean enableLoanApprovalChecker,
    Boolean enableLoanDisbursementChecker,

    @NotNull(message = "transactionCheckerThreshold is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Transaction threshold cannot be negative")
    BigDecimal transactionCheckerThreshold,

    @NotNull(message = "dailyAccountLimitThreshold is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Daily account limit cannot be negative")
    BigDecimal dailyAccountLimitThreshold,

    Boolean enforceAntiSelfApproval
) {}
