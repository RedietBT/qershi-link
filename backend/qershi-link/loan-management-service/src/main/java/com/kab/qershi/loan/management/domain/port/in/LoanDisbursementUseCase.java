package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.InterestType;
import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.RepaymentFrequency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound Port for Loan Disbursement & Account Activation Use Case.
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
            RepaymentFrequency repaymentFrequency,
            InterestType interestType,
            UUID targetSavingsAccountId
    ) {}

    LoanAccount disburseLoan(DisburseCommand command);
}
