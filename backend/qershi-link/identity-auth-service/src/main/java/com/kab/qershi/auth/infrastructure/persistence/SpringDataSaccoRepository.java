package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Technical Data Engine interfacing operations directly with the shared SACCO registry structure.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Repository
public interface SpringDataSaccoRepository extends JpaRepository<SaccoEntity, UUID> {
    boolean existsBySaccoName(String saccoName);
    boolean existsBySchemaName(String schemaName);
}