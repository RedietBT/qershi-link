package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for EodBatchExecutionEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataEodBatchExecutionRepository extends JpaRepository<EodBatchExecutionEntity, UUID> {

    List<EodBatchExecutionEntity> findAllByOrderByStartedAtDesc();

    Optional<EodBatchExecutionEntity> findTopByOrderByStartedAtDesc();

    @Query("SELECT e FROM EodBatchExecutionEntity e WHERE e.businessDate = :businessDate ORDER BY e.startedAt DESC LIMIT 1")
    Optional<EodBatchExecutionEntity> findByBusinessDate(LocalDate businessDate);
}
