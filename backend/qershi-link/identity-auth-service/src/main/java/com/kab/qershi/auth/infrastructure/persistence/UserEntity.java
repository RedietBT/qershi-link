package com.kab.qershi.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

/**
 * Infrastructure JPA Entity mapping directly to the master_schema.users identity table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
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
    private String globalRole;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}