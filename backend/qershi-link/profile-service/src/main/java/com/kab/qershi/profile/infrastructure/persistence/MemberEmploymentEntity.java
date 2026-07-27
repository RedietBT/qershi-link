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
import java.util.UUID;

/**
 * JPA Entity mapping for member_employments database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "member_employments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberEmploymentEntity {

    @Id
    @Column(name = "employment_id", nullable = false, updatable = false)
    private UUID employmentId;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "occupation_sector", nullable = false, length = 100)
    private String occupationSector;

    @Column(name = "employer_name", length = 200)
    private String employerName;

    @Column(name = "monthly_income", nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyIncome;

    @Column(name = "tin_number", length = 30)
    private String tinNumber;
}
