package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SpringDataPermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    // Standard JpaRepository methods (findById, findAll, etc.) are automatically provided
}