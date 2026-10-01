package com.kab.qershi.pricing.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity mapping interest_tax_deduction_logs table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "interest_tax_deduction_logs", indexes = {
        @Index(name = "idx_itdl_account_no", columnList = "account_no"),
        @Index(name = "idx_itdl_business_date", columnList = "business_date")
})
public class InterestTaxDeductionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "account_no", nullable = false, length = 50)
    private String accountNo;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "gross_interest", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossInterest;

    @Column(name = "tax_rate_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRatePct = new BigDecimal("5.00");

    @Column(name = "tax_withheld", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxWithheld;

    @Column(name = "net_interest", nullable = false, precision = 19, scale = 4)
    private BigDecimal netInterest;

    @Column(name = "wht_gl_code", nullable = false, length = 50)
    private String whtGlCode = "2091";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InterestTaxDeductionLogEntity() {}

    public InterestTaxDeductionLogEntity(String accountNo, LocalDate businessDate,
                                         BigDecimal grossInterest, BigDecimal taxRatePct,
                                         BigDecimal taxWithheld, BigDecimal netInterest, String whtGlCode) {
        this(null, accountNo, businessDate, grossInterest, taxRatePct, taxWithheld, netInterest, whtGlCode);
    }

    public InterestTaxDeductionLogEntity(UUID logId, String accountNo, LocalDate businessDate,
                                         BigDecimal grossInterest, BigDecimal taxRatePct,
                                         BigDecimal taxWithheld, BigDecimal netInterest, String whtGlCode) {
        this.logId = logId;
        this.accountNo = accountNo;
        this.businessDate = businessDate;
        this.grossInterest = grossInterest;
        this.taxRatePct = taxRatePct != null ? taxRatePct : new BigDecimal("5.00");
        this.taxWithheld = taxWithheld;
        this.netInterest = netInterest;
        this.whtGlCode = whtGlCode != null ? whtGlCode : "2091";
        this.createdAt = Instant.now();
    }

    public UUID getLogId() { return logId; }
    public String getAccountNo() { return accountNo; }
    public LocalDate getBusinessDate() { return businessDate; }
    public BigDecimal getGrossInterest() { return grossInterest; }
    public BigDecimal getTaxRatePct() { return taxRatePct; }
    public BigDecimal getTaxWithheld() { return taxWithheld; }
    public BigDecimal getNetInterest() { return netInterest; }
    public String getWhtGlCode() { return whtGlCode; }
    public Instant getCreatedAt() { return createdAt; }
}
