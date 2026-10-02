package com.kab.qershi.auth.domain.ports.inbound;

import com.kab.qershi.auth.domain.model.Sacco;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port for administrative lookups on registered SACCO workspace records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SaccoManagementUseCase {

    List<Sacco> getAllSaccos();

    Optional<Sacco> getSaccoById(UUID id);
}
