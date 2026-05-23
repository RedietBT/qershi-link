package com.kab.qershi.auth.domain.model;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User {
    private final UUID userId;
    private String msisdn; // Managed by custom setter for safety
    private final UUID saccoId;
    private String credentialHash;
    private GlobalRole globalRole;
    private UserStatus status;
    private int failedLoginAttempts;
    private Instant lastLoginAt;

    public User(UUID userId, String msisdn, UUID saccoId, String credentialHash, GlobalRole globalRole) {
        this.userId = userId != null ? userId : UUID.randomUUID();
        setMsisdn(msisdn); // Triggers formatting defense block
        this.saccoId = saccoId;
        this.credentialHash = credentialHash;
        this.globalRole = globalRole;
        this.status = UserStatus.PENDING_APPROVAL;
        this.failedLoginAttempts = 0;
    }

    // Explicit Security Setter: Force E.164 formatting for Ethiopian numbers
    public void setMsisdn(String msisdn) {
        if (msisdn == null || !msisdn.matches("^\\+251\\d{9}$")) {
            throw new IllegalArgumentException("Phone number must comply with E.164 format (+251XXXXXXXXX)");
        }
        this.msisdn = msisdn;
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
}