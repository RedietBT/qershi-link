package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping for profile_audit_logs database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "profile_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAuditLogEntity {

    @Id
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "modified_by_user_id", nullable = false, updatable = false)
    private UUID modifiedByUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "field_changed", length = 100)
    private String fieldChanged;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}
