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
@Setter
public class MemberEmployment {

    private final UUID employmentId;
    private final UUID userId;
    private String occupationSector;
    private String employerName;
    private BigDecimal monthlyIncome;
    private String tinNumber;
    private String employeeId;
    private String externalEmployeeId;

    public MemberEmployment(UUID employmentId, UUID userId, String occupationSector,
                            String employerName, BigDecimal monthlyIncome, String tinNumber) {
        this(employmentId, userId, occupationSector, employerName, monthlyIncome, tinNumber, null, null);
    }

    public MemberEmployment(UUID employmentId, UUID userId, String occupationSector,
                            String employerName, BigDecimal monthlyIncome, String tinNumber,
                            String employeeId, String externalEmployeeId) {
        this.employmentId = employmentId != null ? employmentId : UUID.randomUUID();
        this.userId = userId;
        this.occupationSector = occupationSector;
        this.employerName = employerName;
        this.monthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        this.tinNumber = tinNumber;
        this.employeeId = employeeId;
        this.externalEmployeeId = externalEmployeeId;
    }

    public UUID getEmploymentId() { return employmentId; }
    public UUID getUserId() { return userId; }

    public String getOccupationSector() { return occupationSector; }
    public void setOccupationSector(String occupationSector) { this.occupationSector = occupationSector; }

    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getExternalEmployeeId() { return externalEmployeeId; }
    public void setExternalEmployeeId(String externalEmployeeId) { this.externalEmployeeId = externalEmployeeId; }
}
