package com.kab.qershi.transaction.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for sacco_xxx.transaction_audit_logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTransactionAuditLogRepository extends JpaRepository<TransactionAuditLogEntity, UUID> {

    List<TransactionAuditLogEntity> findByTransactionRefOrderByCreatedAtDesc(String transactionRef);

    List<TransactionAuditLogEntity> findByAccountNoOrderByCreatedAtDesc(String accountNo);

    List<TransactionAuditLogEntity> findByPerformedByUserIdOrderByCreatedAtDesc(UUID performedByUserId);

    Page<TransactionAuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
