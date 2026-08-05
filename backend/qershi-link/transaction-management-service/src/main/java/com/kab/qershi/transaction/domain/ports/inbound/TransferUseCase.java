package com.kab.qershi.transaction.domain.ports.inbound;

import com.kab.qershi.transaction.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound port interface declaring member-to-member internal transfer capabilities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransferUseCase {

    Transaction processTransfer(String senderAccountNo, String receiverAccountNo, BigDecimal amount,
                                String narration, String idempotencyKey, UUID processedByUserId);
}
