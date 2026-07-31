package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
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

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "external_employee_id", length = 100)
    private String externalEmployeeId;

    public MemberEmploymentEntity() {}

    public MemberEmploymentEntity(UUID employmentId, UUID userId, String occupationSector,
                                  String employerName, BigDecimal monthlyIncome, String tinNumber) {
        this(employmentId, userId, occupationSector, employerName, monthlyIncome, tinNumber, null, null);
    }

    public MemberEmploymentEntity(UUID employmentId, UUID userId, String occupationSector,
                                  String employerName, BigDecimal monthlyIncome, String tinNumber,
                                  String employeeId, String externalEmployeeId) {
        this.employmentId = employmentId;
        this.userId = userId;
        this.occupationSector = occupationSector;
        this.employerName = employerName;
        this.monthlyIncome = monthlyIncome;
        this.tinNumber = tinNumber;
        this.employeeId = employeeId;
        this.externalEmployeeId = externalEmployeeId;
    }

    public UUID getEmploymentId() { return employmentId; }
    public void setEmploymentId(UUID employmentId) { this.employmentId = employmentId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

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
