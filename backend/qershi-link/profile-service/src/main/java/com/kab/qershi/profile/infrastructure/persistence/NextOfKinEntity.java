package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping for next_of_kin database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "next_of_kin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NextOfKinEntity {

    @Id
    @Column(name = "kin_id", nullable = false, updatable = false)
    private UUID kinId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "primary_phone", nullable = false, length = 15)
    private String primaryPhone;

    @Column(name = "id_number", length = 100)
    private String idNumber;

    @Column(name = "physical_address", length = 255)
    private String physicalAddress;

    @Column(name = "allocation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
