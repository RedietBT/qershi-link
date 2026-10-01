package com.kab.qershi.pricing.domain.ports.outbound;

import com.kab.qershi.pricing.domain.model.Tariff;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound SPI port for persisting tariff configurations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TariffRepositoryPort {
    List<Tariff> findAll();
    List<Tariff> findActiveByTransactionType(String transactionType);
    Optional<Tariff> findById(UUID tariffId);
    Optional<Tariff> findByTariffCode(String tariffCode);
    Tariff save(Tariff tariff);
}
