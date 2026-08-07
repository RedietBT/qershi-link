package com.kab.qershi.loan.origination.infrastructure.rest.dto;

import com.kab.qershi.loan.origination.domain.ports.inbound.LoanApplicationUseCase.CollateralInput;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST request DTO for submitting a loan application.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanApplicationRequest(
        @NotNull(message = "Borrower user ID is required")
        UUID userId,

        UUID groupId,

        @NotNull(message = "Loan product ID is required")
        UUID productId,

        String scoringType,

        @NotNull(message = "Amount requested is required")
        @Positive(message = "Amount requested must be greater than zero")
        BigDecimal amountRequested,

        BigDecimal savingsConsistency,
        BigDecimal historicalYield,
        BigDecimal projectedYield,
        BigDecimal landSizeHectares,
        List<CollateralInput> collaterals
) {}
