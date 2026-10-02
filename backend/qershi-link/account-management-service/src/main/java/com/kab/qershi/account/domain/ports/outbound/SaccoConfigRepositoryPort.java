package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.SaccoConfig;

import java.util.Optional;

/**
 * Outbound repository port for SACCO configuration persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SaccoConfigRepositoryPort {

    SaccoConfig save(SaccoConfig config);

    Optional<SaccoConfig> findFirst();
}
