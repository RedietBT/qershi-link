package com.kab.qershi.profile.infrastructure.persistence;

import com.kab.qershi.profile.domain.model.IdType;
import com.kab.qershi.profile.domain.model.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA Entity mapping for member_identifications database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "member_identifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdentificationEntity {

    @Id
    @Column(name = "identification_id", nullable = false, updatable = false)
    private UUID identificationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;

    @Column(name = "id_number", nullable = false, length = 100)
    private String idNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority", length = 150)
    private String issuingAuthority;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus;

    @Column(name = "verified_by_user_id")
    private UUID verifiedByUserId;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
