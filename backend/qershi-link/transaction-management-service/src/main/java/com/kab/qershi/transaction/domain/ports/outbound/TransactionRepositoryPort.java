package com.kab.qershi.transaction.domain.ports.outbound;

import com.kab.qershi.transaction.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for transaction CRUD and idempotency checks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByTransactionRef(String transactionRef);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findByAccountNo(String accountNo);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
