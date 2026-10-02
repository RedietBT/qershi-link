package com.kab.qershi.auth.domain.ports.inbound;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for managing user security profiles and administrative role mappings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface UserManagementUseCase {

    List<User> getAllUsers(UUID saccoId, boolean isSuperAdmin, UUID tenantSaccoId);

    User getUserById(UUID id, boolean isSuperAdmin, UUID tenantSaccoId);

    String createUser(String msisdn, GlobalRole globalRole, UUID targetSaccoId);

    User updateUser(UUID id, String msisdn, UserStatus status, boolean isSuperAdmin, UUID tenantSaccoId);

    void deleteUser(UUID id);

    void assignRole(UUID userId, UUID roleId, UUID targetSaccoId);
}
