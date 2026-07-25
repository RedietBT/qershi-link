package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByMsisdn(String msisdn);

    // Updated to include saccoId
    void saveSuperAdmin(String userId, String msisdn, String hashedPin, String role, String saccoId);

    // Updated to include saccoId
    void assignRole(String userId, String roleId, String saccoId);
}