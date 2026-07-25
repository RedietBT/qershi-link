package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, UUID> {
    // Any custom query methods
}