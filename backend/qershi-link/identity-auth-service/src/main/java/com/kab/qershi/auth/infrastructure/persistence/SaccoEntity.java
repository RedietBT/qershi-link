package com.kab.qershi.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Infrastructure JPA Entity mapping directly to the master_schema.sacco_registry table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "sacco_registry", schema = "master_schema")
@Getter
@Setter
public class SaccoEntity {

    @Id
    @Column(name = "sacco_id", updatable = false, nullable = false)
    private UUID saccoId;

    @Column(name = "parent_union_id")
    private UUID parentUnionId;

    @Column(name = "sacco_name", nullable = false)
    private String saccoName;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    @Column(name = "is_union", nullable = false)
    private boolean isUnion;

    @Column(name = "min_share_requirement", precision = 19, scale = 4)
    private BigDecimal minShareRequirement;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}