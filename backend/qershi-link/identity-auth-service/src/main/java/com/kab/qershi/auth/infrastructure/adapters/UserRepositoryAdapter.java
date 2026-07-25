package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapToEntity(user);
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
        // Updated to include saccoId to match the new multi-tenant database schema
        repository.insertUserRole(UUID.fromString(userId), UUID.fromString(roleId), UUID.fromString(saccoId));
    }

    @Override
    public void saveSuperAdmin(String userId, String msisdn, String hashedPin, String role, String saccoId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(UUID.fromString(userId));
        entity.setMsisdn(msisdn);
        entity.setSaccoId(UUID.fromString(saccoId)); // Ensure this is captured
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

    // --- Data Mappers ---
    private User mapToDomain(UserEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getUserId(),
                entity.getMsisdn(),
                entity.getSaccoId(),
                entity.getCredentialHash(),
                entity.getGlobalRole()
        );
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