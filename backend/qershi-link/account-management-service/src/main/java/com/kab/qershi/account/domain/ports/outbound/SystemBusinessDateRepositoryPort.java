package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.SystemBusinessDate;

import java.util.Optional;

/**
 * Outbound repository port for managing Core Banking System Business Date persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface SystemBusinessDateRepositoryPort {

    SystemBusinessDate save(SystemBusinessDate businessDate);

    Optional<SystemBusinessDate> findCurrentBusinessDate();
}
