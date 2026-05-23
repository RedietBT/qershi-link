package com.kab.qershi.auth.infrastructure.persistence;

import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Hexagonal Outbound Adapter translating core identity records between domain models and JPA tables.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId());
        entity.setMsisdn(user.getMsisdn());
        entity.setSaccoId(user.getSaccoId());
        entity.setCredentialHash(user.getCredentialHash());
        entity.setGlobalRole(user.getGlobalRole().name());
        entity.setStatus(user.getStatus().name());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLastLoginAt(user.getLastLoginAt());

        repository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return repository.findById(userId).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByMsisdn(String msisdn) {
        return repository.findByMsisdn(msisdn).map(this::mapToDomain);
    }

    private User mapToDomain(UserEntity entity) {
        return new User(
                entity.getUserId(),
                entity.getMsisdn(),
                entity.getSaccoId(),
                entity.getCredentialHash(),
                GlobalRole.valueOf(entity.getGlobalRole())
        );
    }
}