package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.Sacco;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaccoRepositoryPort {
    Sacco save(Sacco sacco);
    Optional<Sacco> findById(UUID saccoId);
    List<Sacco> findAll();
    boolean existsBySaccoName(String saccoName);
    boolean existsBySchemaName(String schemaName);
}