package com.kab.qershi.transaction.domain.ports.outbound;

import java.math.BigDecimal;

/**
 * Outbound port interface declaring inter-service RPC communication with account-management-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountClientPort {

    AccountInfo getAccountInfo(String accountNo);

    ValidationResult validateDebit(String accountNo, BigDecimal amount);

    ValidationResult validateCredit(String accountNo, BigDecimal amount);

    boolean postTransaction(String accountNo, BigDecimal amount, String transactionType);

    record AccountInfo(
            String accountId,
            String accountNo,
            String userId,
            String saccoCode,
            String branchCode,
            String productCode,
            BigDecimal bookBalance,
            BigDecimal lienHoldAmount,
            BigDecimal availableBalance,
            String status,
            String freezeStatus,
            String phoneNumber,
            String fullName
    ) {}

    record ValidationResult(
            boolean isValid,
            String message,
            BigDecimal availableBalance
    ) {}
}
