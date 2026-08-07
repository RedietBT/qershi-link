package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;
import com.kab.qershi.loan.management.domain.model.PaymentChannel;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound Port for Loan Repayment Processing Use Case.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanRepaymentUseCase {

    record RepaymentCommand(
            UUID accountId,
            BigDecimal amount,
            PaymentChannel paymentChannel,
            String remarks
    ) {}

    LoanRepayment processRepayment(RepaymentCommand command);
}
