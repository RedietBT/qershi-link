package com.kab.qershi.pricing.application.usecase;

import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.ports.inbound.TariffManagementUseCase;
import com.kab.qershi.pricing.domain.ports.outbound.TariffRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for configuring and administering tariff schedules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TariffManagementService implements TariffManagementUseCase {

    private final TariffRepositoryPort tariffRepositoryPort;

    public TariffManagementService(TariffRepositoryPort tariffRepositoryPort) {
        this.tariffRepositoryPort = tariffRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tariff> listAllTariffs() {
        return tariffRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public Tariff createTariff(Tariff tariff) {
        if (tariff.getTariffCode() == null || tariff.getTariffCode().isBlank()) {
            throw new IllegalArgumentException("Tariff code is required.");
        }
        String cleanCode = tariff.getTariffCode().trim().toUpperCase();
        if (tariffRepositoryPort.findByTariffCode(cleanCode).isPresent()) {
            throw new IllegalArgumentException("Tariff with code " + cleanCode + " already exists.");
        }
        tariff.setTariffCode(cleanCode);
        tariff.setCreatedAt(Instant.now());
        tariff.setUpdatedAt(Instant.now());
        return tariffRepositoryPort.save(tariff);
    }

    @Override
    @Transactional
    public Tariff updateTariff(UUID tariffId, Tariff updated) {
        Tariff existing = tariffRepositoryPort.findById(tariffId)
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with ID: " + tariffId));

        existing.setTariffName(updated.getTariffName());
        existing.setTransactionType(updated.getTransactionType());
        existing.setFeeType(updated.getFeeType());
        existing.setFeeValue(updated.getFeeValue());
        existing.setMinFee(updated.getMinFee());
        existing.setMaxFee(updated.getMaxFee());
        existing.setFeeGlCode(updated.getFeeGlCode());
        existing.setActive(updated.isActive());
        existing.setDescription(updated.getDescription());
        existing.setUpdatedAt(Instant.now());

        return tariffRepositoryPort.save(existing);
    }

    @Override
    @Transactional
    public Tariff toggleTariffStatus(UUID tariffId, boolean active) {
        Tariff existing = tariffRepositoryPort.findById(tariffId)
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with ID: " + tariffId));
        existing.setActive(active);
        existing.setUpdatedAt(Instant.now());
        return tariffRepositoryPort.save(existing);
    }
}
