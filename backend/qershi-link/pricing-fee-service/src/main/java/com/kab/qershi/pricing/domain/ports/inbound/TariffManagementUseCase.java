package com.kab.qershi.pricing.domain.ports.inbound;

import com.kab.qershi.pricing.domain.model.Tariff;
import java.util.List;
import java.util.UUID;

/**
 * Inbound port for managing SACCO tariff rules and policies.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TariffManagementUseCase {
    List<Tariff> listAllTariffs();
    Tariff createTariff(Tariff tariff);
    Tariff updateTariff(UUID tariffId, Tariff tariff);
    Tariff toggleTariffStatus(UUID tariffId, boolean active);
}
