package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
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

    List<TariffEntity> findByTransactionTypeAndActiveTrue(String transactionType);

    List<TariffEntity> findByActiveTrue();
}
