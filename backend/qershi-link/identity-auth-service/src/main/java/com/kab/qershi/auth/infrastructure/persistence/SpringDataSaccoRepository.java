package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SpringDataSaccoRepository extends JpaRepository<SaccoEntity, UUID> {
    boolean existsBySaccoName(String saccoName);
    boolean existsBySchemaName(String schemaName);
}