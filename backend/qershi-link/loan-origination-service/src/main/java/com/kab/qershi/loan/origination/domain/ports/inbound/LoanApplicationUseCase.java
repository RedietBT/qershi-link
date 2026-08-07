package com.kab.qershi.loan.origination.domain.ports.inbound;

import com.kab.qershi.loan.origination.domain.model.LoanApplication;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound port for submitting and querying SACCO loan applications.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public interface LoanApplicationUseCase {

    record CollateralInput(
            String type,
            BigDecimal estimatedValue,
            String documentUrl
    ) {}

    record SubmitApplicationCommand(
            UUID userId,
            UUID groupId,
            UUID productId,
            String scoringType,
            BigDecimal amountRequested,
            BigDecimal savingsConsistency,
            BigDecimal historicalYield,
            BigDecimal projectedYield,
            BigDecimal landSizeHectares,
            List<CollateralInput> collaterals
    ) {}

    LoanApplication submitApplication(SubmitApplicationCommand command);

    LoanApplication getApplicationById(UUID applicationId);

    List<LoanApplication> listApplicationsForUser(UUID userId);
}
