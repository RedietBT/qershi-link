package com.kab.qershi.transaction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository interface for 'till_cash_reconciliations' table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTillCashReconciliationRepository extends JpaRepository<TillCashReconciliationEntity, UUID> {

    List<TillCashReconciliationEntity> findByTillIdOrderByCreatedAtDesc(UUID tillId);

    List<TillCashReconciliationEntity> findByTellerUserIdOrderByCreatedAtDesc(UUID tellerUserId);
}
