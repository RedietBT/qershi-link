package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity representing a member's economic sector, employer, and tax handles.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
public class MemberEmployment {

    private final UUID employmentId;
    private final UUID userId;
    @Setter private String occupationSector;
    @Setter private String employerName;
    @Setter private BigDecimal monthlyIncome;
    @Setter private String tinNumber;

    public MemberEmployment(UUID employmentId, UUID userId, String occupationSector,
                            String employerName, BigDecimal monthlyIncome, String tinNumber) {
        this.employmentId = employmentId != null ? employmentId : UUID.randomUUID();
        this.userId = userId;
        this.occupationSector = occupationSector;
        this.employerName = employerName;
        this.monthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        this.tinNumber = tinNumber;
    }
}
