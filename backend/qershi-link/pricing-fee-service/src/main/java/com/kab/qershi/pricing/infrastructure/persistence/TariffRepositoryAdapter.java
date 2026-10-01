package com.kab.qershi.pricing.infrastructure.persistence;

import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.ports.outbound.TariffRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing TariffRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TariffRepositoryAdapter implements TariffRepositoryPort {

    private final SpringDataTariffRepository repository;

    public TariffRepositoryAdapter(SpringDataTariffRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Tariff> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Tariff> findActiveByTransactionType(String transactionType) {
        return repository.findByTransactionTypeAndActiveTrue(transactionType).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Tariff> findById(UUID tariffId) {
        return repository.findById(tariffId).map(this::toDomain);
    }

    @Override
    public Optional<Tariff> findByTariffCode(String tariffCode) {
        return repository.findByTariffCode(tariffCode).map(this::toDomain);
    }

    @Override
    public Tariff save(Tariff tariff) {
        TariffEntity entity = toEntity(tariff);
        TariffEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private Tariff toDomain(TariffEntity entity) {
        if (entity == null) return null;
        Tariff domain = new Tariff(
                entity.getTariffId(),
                entity.getTariffCode(),
                entity.getTariffName(),
                entity.getTransactionType(),
                entity.getFeeType(),
                entity.getFeeValue(),
                entity.getMinFee(),
                entity.getMaxFee(),
                entity.getFeeGlCode(),
                entity.getCurrency(),
                entity.isActive(),
                entity.getDescription()
        );
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private TariffEntity toEntity(Tariff domain) {
        if (domain == null) return null;
        return new TariffEntity(
                domain.getTariffId(),
                domain.getTariffCode(),
                domain.getTariffName(),
                domain.getTransactionType(),
                domain.getFeeType(),
                domain.getFeeValue(),
                domain.getMinFee(),
                domain.getMaxFee(),
                domain.getFeeGlCode(),
                domain.getCurrency(),
                domain.isActive(),
                domain.getDescription()
        );
    }
}
