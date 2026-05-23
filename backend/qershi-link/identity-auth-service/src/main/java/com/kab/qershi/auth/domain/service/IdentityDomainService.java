package com.kab.qershi.auth.domain.service;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;

public class IdentityDomainService {

    private final SaccoRepositoryPort saccoRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public IdentityDomainService(SaccoRepositoryPort saccoRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    /**
     * Technical Requirement Section 1.1.1.3.3: Schema Naming Convention
     * Programmatically sanitizes names to prevent SQL injection during dynamic search_path routing.
     */
    public String generateAndSanitizeSchemaName(String saccoName, boolean isUnion) {
        if (saccoName == null || saccoName.isBlank()) {
            throw new IllegalArgumentException("SACCO name cannot be empty for schema generation.");
        }

        // Lowercase, no spaces, strip special characters out to maintain strict naming rules
        String cleanName = saccoName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_");

        String prefix = isUnion ? "union_" : "sacco_";
        String tentativeSchema = prefix + cleanName;

        if (saccoRepositoryPort.existsBySchemaName(tentativeSchema)) {
            throw new IllegalStateException("Database routing conflict: Schema name '" + tentativeSchema + "' already exists.");
        }

        return tentativeSchema;
    }

    /**
     * Business Rule Section 1.1.1.6.1: Immutable Mapping Strategy
     * Checks if an identity record or volatile handle already conflicts.
     */
    public void validateUniqueUserIdentifiers(String msisdn) {
        if (userRepositoryPort.findByMsisdn(msisdn).isPresent()) {
            throw new IllegalStateException("A global identity handle already exists for MSISDN: " + msisdn);
        }
    }
}