package com.kab.qershi.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Technical Data Engine interfacing operations directly with the isolated tenant security roles matrices.
 *
 * @author KAB Digital Solution PLC
 * @version 1.3.0
 */
@Repository
public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, UUID> {

    boolean existsByRoleName(String roleName);

    @Query(value = "SELECT COUNT(*) FROM master_schema.user_roles WHERE role_id = :roleId", nativeQuery = true)
    long countUsersAssignedToRole(@Param("roleId") UUID roleId);
}