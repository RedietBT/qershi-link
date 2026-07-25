package com.kab.qershi.auth.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Sacco {

    private UUID saccoId;

    private UUID parentUnionId;

    private String saccoName;

    private final String schemaName;

    private final boolean isUnion;

    private BigDecimal minShareRequirement;

    private SaccoStatus status;

    private final Instant createdAt;

    private Instant updatedAt;

    protected Sacco() {
        this.schemaName = null;
        this.isUnion = false;
        this.createdAt = Instant.now();
    }

    public Sacco(
            UUID saccoId,
            String saccoName,
            String schemaName,
            boolean isUnion,
            BigDecimal minShareRequirement
    ) {
        this.saccoId = saccoId != null ? saccoId : UUID.randomUUID();
        this.saccoName = saccoName;
        this.schemaName = schemaName;
        this.isUnion = isUnion;
        this.minShareRequirement =
                minShareRequirement != null
                        ? minShareRequirement
                        : BigDecimal.ZERO;

        this.status = SaccoStatus.PENDING_SETUP;

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void attachToUnion(UUID unionId) {

        if (this.saccoId.equals(unionId)) {
            throw new IllegalArgumentException(
                    "A SACCO cannot be its own parent Union."
            );
        }

        this.parentUnionId = unionId;
        this.updatedAt = Instant.now();
    }

    public void activate() {

        this.status = SaccoStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }
}