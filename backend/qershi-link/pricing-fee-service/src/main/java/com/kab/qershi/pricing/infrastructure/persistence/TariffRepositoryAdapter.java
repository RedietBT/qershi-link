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
        TariffEntity entity;
        if (tariff.getTariffId() != null) {
            Optional<TariffEntity> opt = repository.findById(tariff.getTariffId());
            if (opt.isPresent()) {
                entity = opt.get();
                entity.setTariffCode(tariff.getTariffCode());
                entity.setTariffName(tariff.getTariffName());
                entity.setTransactionType(tariff.getTransactionType());
                entity.setFeeType(tariff.getFeeType());
                entity.setFeeValue(tariff.getFeeValue());
                entity.setMinFee(tariff.getMinFee());
                entity.setMaxFee(tariff.getMaxFee());
                entity.setFeeGlCode(tariff.getFeeGlCode());
                entity.setCurrency(tariff.getCurrency());
                entity.setActive(tariff.isActive());
                entity.setDescription(tariff.getDescription());

                List<TariffSlabEntity> slabEntities = tariff.getSlabs() != null
                        ? tariff.getSlabs().stream().map(s -> slabToEntity(s, entity)).collect(Collectors.toList())
                        : new java.util.ArrayList<>();
                entity.setSlabs(slabEntities);
            } else {
                entity = toEntity(tariff);
            }
        } else {
            entity = toEntity(tariff);
        }
        TariffEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private Tariff toDomain(TariffEntity entity) {
        if (entity == null) return null;
        List<com.kab.qershi.pricing.domain.model.TariffSlab> domainSlabs = entity.getSlabs() != null
                ? entity.getSlabs().stream().map(this::slabToDomain).collect(Collectors.toList())
                : new java.util.ArrayList<>();

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
                entity.getDescription(),
                domainSlabs
        );
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private TariffEntity toEntity(Tariff domain) {
        if (domain == null) return null;
        TariffEntity entity = new TariffEntity(
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
        if (domain.getSlabs() != null) {
            List<TariffSlabEntity> slabEntities = domain.getSlabs().stream()
                    .map(s -> slabToEntity(s, entity))
                    .collect(Collectors.toList());
            entity.setSlabs(slabEntities);
        }
        return entity;
    }

    private com.kab.qershi.pricing.domain.model.TariffSlab slabToDomain(TariffSlabEntity s) {
        if (s == null) return null;
        return new com.kab.qershi.pricing.domain.model.TariffSlab(
                s.getSlabId(),
                s.getTariff() != null ? s.getTariff().getTariffId() : null,
                s.getSlabOrder(),
                s.getFromAmount(),
                s.getToAmount(),
                s.getFeeType(),
                s.getFeeValue(),
                s.getMinFee(),
                s.getMaxFee()
        );
    }

    private TariffSlabEntity slabToEntity(com.kab.qershi.pricing.domain.model.TariffSlab s, TariffEntity tariffEntity) {
        if (s == null) return null;
        return new TariffSlabEntity(
                s.getSlabId(),
                tariffEntity,
                s.getSlabOrder(),
                s.getFromAmount(),
                s.getToAmount(),
                s.getFeeType(),
                s.getFeeValue(),
                s.getMinFee(),
                s.getMaxFee()
        );
    }
}
