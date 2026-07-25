package com.kab.qershi.auth.infrastructure.persistence;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "master_schema")
@Getter
@Setter
public class UserEntity {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "msisdn", nullable = false, unique = true, length = 15)
    private String msisdn;

    @Column(name = "sacco_id", nullable = false)
    private UUID saccoId;

    @Column(name = "credential_hash", nullable = false, columnDefinition = "TEXT")
    private String credentialHash;

    @Column(name = "global_role", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // Binds domain enum directly to PG native enum
    private GlobalRole globalRole;

    @Column(name = "status", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM) // Binds domain enum directly to PG native enum
    private UserStatus status;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> localRoles = new HashSet<>();
}