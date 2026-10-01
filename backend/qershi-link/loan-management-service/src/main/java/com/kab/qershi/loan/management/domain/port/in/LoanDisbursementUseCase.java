package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Port for Loan Disbursement & Account Activation Use Case (Dynamic Tier-1 Standards).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanDisbursementUseCase {

    record GuarantorDisbursementInput(
            UUID guarantorUserId,
            String guarantorName,
            String guarantorPhone,
            String savingsAccountNo,
            BigDecimal guaranteedAmount
    ) {}

    record DisburseCommand(
            UUID applicationId,
            UUID userId,
            UUID productId,
            BigDecimal amount,
            BigDecimal interestRatePct,
            Integer termMonths,
            String repaymentFrequency,
            String interestType,
            UUID targetSavingsAccountId,
            String memberPhone,
            String idempotencyKey,
            List<GuarantorDisbursementInput> guarantors
    ) {
        public DisburseCommand(
                UUID applicationId,
                UUID userId,
                UUID productId,
                BigDecimal amount,
                BigDecimal interestRatePct,
                Integer termMonths,
                String repaymentFrequency,
                String interestType,
                UUID targetSavingsAccountId,
                String memberPhone,
                String idempotencyKey
        ) {
            this(applicationId, userId, productId, amount, interestRatePct, termMonths,
                 repaymentFrequency, interestType, targetSavingsAccountId, memberPhone, idempotencyKey, List.of());
        }
    }

    LoanAccount disburseLoan(DisburseCommand command);

    LoanAccount approveDisbursement(UUID accountId, UUID checkerUserId);
}
