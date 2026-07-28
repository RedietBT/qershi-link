package com.kab.qershi.auth.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {
    private final UUID userId;
    private String msisdn; // Managed by custom setter for safety
    private final UUID saccoId;
    private String credentialHash;
    private GlobalRole globalRole;
    private final Set<Role> localRoles; // Alignment: Tracks tenant-specific RBAC assignments
    private UserStatus status;
    private int failedLoginAttempts;
    private Instant lastLoginAt;

    public User(UUID userId, String msisdn, UUID saccoId, String credentialHash, GlobalRole globalRole) {
        this.userId = userId != null ? userId : UUID.randomUUID();
        setMsisdn(msisdn); // Triggers formatting defense block
        this.saccoId = saccoId;
        this.credentialHash = credentialHash;
        this.globalRole = globalRole;
        this.localRoles = new HashSet<>();
        this.status = UserStatus.PENDING_APPROVAL;
        this.failedLoginAttempts = 0;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public UUID getSaccoId() {
        return saccoId;
    }

    public String getCredentialHash() {
        return credentialHash;
    }

    public GlobalRole getGlobalRole() {
        return globalRole;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    // Explicit Security Setter: Force E.164 formatting for Ethiopian numbers
    public void setMsisdn(String msisdn) {
        if (msisdn == null || !msisdn.matches("^\\+251\\d{9}$")) {
            throw new IllegalArgumentException("Phone number must comply with E.164 format (+251XXXXXXXXX)");
        }
        this.msisdn = msisdn;
    }

    // Business Logic: Dynamic Role Assignment
    public void assignLocalRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Cannot assign a null role to a user.");
        }
        this.localRoles.add(role);
    }

    public void revokeLocalRole(Role role) {
        this.localRoles.remove(role);
    }

    public Set<Role> getLocalRoles() {
        return Collections.unmodifiableSet(localRoles);
    }

    public boolean canLogin() {
        return this.status == UserStatus.ACTIVE || this.status == UserStatus.PENDING_SHARE;
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.status = UserStatus.DEACTIVATED;
        }
    }

    public void successfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = Instant.now();
    }

    public void activateMembership() {
        if (this.status == UserStatus.PENDING_SHARE) {
            this.status = UserStatus.ACTIVE;
        }
    }

    public void setCredentialHash(String credentialHash) {
        if (credentialHash == null || credentialHash.isEmpty()) {
            throw new IllegalArgumentException("Credential hash cannot be null or empty.");
        }
        this.credentialHash = credentialHash;
    }

    public void resetLoginAttempts() {
        this.failedLoginAttempts = 0;
    }
}