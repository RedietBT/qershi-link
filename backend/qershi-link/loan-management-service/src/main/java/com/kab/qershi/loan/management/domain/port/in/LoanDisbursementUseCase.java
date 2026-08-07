package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanAccount;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound Port for Loan Disbursement & Account Activation Use Case (Dynamic Tier-1 Standards).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanDisbursementUseCase {

    record DisburseCommand(
            UUID applicationId,
            UUID userId,
            UUID productId,
            BigDecimal amount,
            BigDecimal interestRatePct,
            Integer termMonths,
            String repaymentFrequency,
            String interestType,
            UUID targetSavingsAccountId
    ) {}

    LoanAccount disburseLoan(DisburseCommand command);
}
