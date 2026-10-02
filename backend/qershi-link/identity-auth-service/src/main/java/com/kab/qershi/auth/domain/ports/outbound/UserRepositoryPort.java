package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByMsisdn(String msisdn);
    List<User> findAll();
    List<User> findBySaccoId(UUID saccoId);
    boolean existsById(UUID userId);
    void deleteById(UUID userId);

    void saveSuperAdmin(String userId, String msisdn, String hashedPin, String role, String saccoId);
    void assignRole(String userId, String roleId, String saccoId);
    void insertUserRole(UUID userId, UUID roleId, UUID saccoId);

    // Resolves the full list of permission authority strings for a user in a specific SACCO context
    List<String> findPermissions(UUID userId, UUID saccoId);
}