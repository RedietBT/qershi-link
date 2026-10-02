package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.ports.inbound.SaccoManagementUseCase;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for inspecting and monitoring registered SACCO workspaces.
 * Pure application service with zero infrastructure imports.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class SaccoManagementService implements SaccoManagementUseCase {

    private final SaccoRepositoryPort saccoRepositoryPort;

    public SaccoManagementService(SaccoRepositoryPort saccoRepositoryPort) {
        this.saccoRepositoryPort = saccoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sacco> getAllSaccos() {
        return saccoRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sacco> getSaccoById(UUID id) {
        return saccoRepositoryPort.findById(id);
    }
}
