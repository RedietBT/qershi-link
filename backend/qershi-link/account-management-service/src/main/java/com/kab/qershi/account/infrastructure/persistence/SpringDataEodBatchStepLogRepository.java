package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for EodBatchStepLogEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataEodBatchStepLogRepository extends JpaRepository<EodBatchStepLogEntity, UUID> {

    List<EodBatchStepLogEntity> findByBatchIdOrderByCreatedAtAsc(UUID batchId);
}
