package com.kab.qershi.pricing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for tariff_slabs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTariffSlabRepository extends JpaRepository<TariffSlabEntity, UUID> {
    List<TariffSlabEntity> findByTariff_TariffIdOrderBySlabOrderAscFromAmountAsc(UUID tariffId);
}
