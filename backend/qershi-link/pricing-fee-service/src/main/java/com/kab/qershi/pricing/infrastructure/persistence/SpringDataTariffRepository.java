package com.kab.qershi.pricing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for TariffEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTariffRepository extends JpaRepository<TariffEntity, UUID> {

    Optional<TariffEntity> findByTariffCode(String tariffCode);

    @Query("SELECT t FROM TariffEntity t WHERE t.transactionType = :txnType AND t.active = true ORDER BY t.createdAt DESC")
    List<TariffEntity> findByTransactionTypeAndActiveTrue(@Param("txnType") String txnType);

    List<TariffEntity> findByActive(boolean active);
}
