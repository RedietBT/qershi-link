package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Technical Data Engine interfacing operations directly with the global system security accounts index.
 * Updated to support multi-tenant role assignment and authority resolution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByMsisdn(String msisdn);

    @Modifying
    @Query(value = "INSERT INTO master_schema.user_roles (user_id, role_id, sacco_id) VALUES (:userId, :roleId, :saccoId)", nativeQuery = true)
    void insertUserRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId, @Param("saccoId") UUID saccoId);

    @Query(value = "SELECT DISTINCT p.action || '_' || p.resource " +
            "FROM master_schema.permissions p " +
            "JOIN master_schema.role_permissions rp ON p.permission_id = rp.permission_id " +
            "JOIN master_schema.user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId AND ur.sacco_id = :saccoId", nativeQuery = true)
    List<String> findAuthoritiesByUserIdAndSaccoId(@Param("userId") UUID userId, @Param("saccoId") UUID saccoId);
}