package com.kab.qershi.transaction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for 'transactions' database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByTransactionRef(String transactionRef);

    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);

    List<TransactionEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
