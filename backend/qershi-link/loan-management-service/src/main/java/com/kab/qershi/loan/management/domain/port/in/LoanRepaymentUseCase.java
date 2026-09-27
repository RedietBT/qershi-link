package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound Port for Loan Repayment Processing Use Case (Dynamic Tier-1 Standards).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanRepaymentUseCase {

    record RepaymentCommand(
            UUID accountId,
            BigDecimal amount,
            String paymentChannel,
            String remarks,
            String memberPhone,
            String sourceAccountNo
    ) {
        public RepaymentCommand(UUID accountId, BigDecimal amount, String paymentChannel, String remarks, String memberPhone) {
            this(accountId, amount, paymentChannel, remarks, memberPhone, null);
        }
    }

    LoanRepayment processRepayment(RepaymentCommand command);
}
