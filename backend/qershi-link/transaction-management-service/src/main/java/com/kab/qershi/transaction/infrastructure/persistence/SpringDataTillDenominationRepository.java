package com.kab.qershi.transaction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTillDenominationRepository extends JpaRepository<TillDenominationEntity, UUID> {
    List<TillDenominationEntity> findByReconciliationIdOrderByDenominationValueDesc(UUID reconciliationId);
}
