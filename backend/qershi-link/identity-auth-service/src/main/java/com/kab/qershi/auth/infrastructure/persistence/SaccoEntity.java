package com.kab.qershi.auth.infrastructure.persistence;

import com.kab.qershi.auth.domain.model.SaccoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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

    @Column(name = "sacco_name", nullable = false, length = 255)
    private String saccoName;

    @Column(name = "schema_name", nullable = false, unique = true, length = 63)
    private String schemaName;

    @Column(name = "is_union", nullable = false)
    private boolean isUnion;

    @Column(name = "min_share_requirement", precision = 19, scale = 4, nullable = false)
    private BigDecimal minShareRequirement;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "master_schema.master_schema_sacco_status"
    )
    private SaccoStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}