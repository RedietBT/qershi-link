package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Adapter bridging Domain Model User operations to Spring Data JPA Persistence entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.5.0
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public User save(User user) {
        // Fetch existing entity to preserve mapped relationships (e.g. localRoles in user_roles)
        UserEntity entity = repository.findById(user.getUserId())
                .orElseGet(() -> mapToEntity(user));

        entity.setMsisdn(user.getMsisdn());
        entity.setSaccoId(user.getSaccoId());
        entity.setCredentialHash(user.getCredentialHash());
        entity.setGlobalRole(user.getGlobalRole());
        entity.setStatus(user.getStatus() != null ? user.getStatus() : UserStatus.PENDING_APPROVAL);
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLastLoginAt(user.getLastLoginAt());

        UserEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return repository.findById(userId)
                .map(this::mapToDomain);
    }

    @Override
    @Transactional
    public void assignRole(String userId, String roleId, String saccoId) {
        repository.insertUserRole(UUID.fromString(userId), UUID.fromString(roleId), UUID.fromString(saccoId));
    }

    @Override
    public void saveSuperAdmin(String userId, String msisdn, String hashedPin, String role, String saccoId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(UUID.fromString(userId));
        entity.setMsisdn(msisdn);
        entity.setSaccoId(UUID.fromString(saccoId));
        entity.setCredentialHash(hashedPin);
        entity.setGlobalRole(GlobalRole.valueOf(role));
        entity.setStatus(UserStatus.ACTIVE);

        repository.save(entity);
    }

    @Override
    public Optional<User> findByMsisdn(String msisdn) {
        return repository.findByMsisdn(msisdn)
                .map(this::mapToDomain);
    }

    @Override
    public List<String> findPermissions(UUID userId, UUID saccoId) {
        return repository.findAuthoritiesByUserIdAndSaccoId(userId, saccoId);
    }

    // --- Data Mappers ---
    private User mapToDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = new User(
                entity.getUserId(),
                entity.getMsisdn(),
                entity.getSaccoId(),
                entity.getCredentialHash(),
                entity.getGlobalRole()
        );
        user.setStatus(entity.getStatus());
        return user;
    }

    private UserEntity mapToEntity(User domain) {
        if (domain == null) return null;

        UserEntity entity = new UserEntity();
        entity.setUserId(domain.getUserId());
        entity.setMsisdn(domain.getMsisdn());
        entity.setSaccoId(domain.getSaccoId());
        entity.setCredentialHash(domain.getCredentialHash());
        entity.setGlobalRole(domain.getGlobalRole());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus() : UserStatus.PENDING_APPROVAL);
        entity.setFailedLoginAttempts(domain.getFailedLoginAttempts());
        entity.setLastLoginAt(domain.getLastLoginAt());

        return entity;
    }
}