package com.kab.qershi.transaction.domain.ports.inbound;

import com.kab.qershi.transaction.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound port interface declaring cash deposit and withdrawal teller capabilities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface CashTransactionUseCase {

    Transaction processDeposit(String accountNo, BigDecimal amount, String narration, String idempotencyKey, UUID processedByUserId);

    Transaction processWithdrawal(String accountNo, BigDecimal amount, String narration, String idempotencyKey, UUID processedByUserId);
}
