package com.kab.qershi.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping till_closing_logs table.
 * Audit trail of historical drawer closures, blind balances, and variances.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "till_closing_logs", indexes = {
        @Index(name = "idx_tcl_till_id", columnList = "till_id"),
        @Index(name = "idx_tcl_teller_user", columnList = "teller_user_id")
})
public class TillClosingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "reconciliation_id", nullable = false)
    private UUID reconciliationId;

    @Column(name = "till_id", nullable = false)
    private UUID tillId;

    @Column(name = "teller_user_id", nullable = false)
    private UUID tellerUserId;

    @Column(name = "closing_mode", nullable = false, length = 20)
    private String closingMode = "BLIND";

    @Column(name = "electronic_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal electronicBalance;

    @Column(name = "physical_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal physicalTotal;

    @Column(name = "variance", nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 40)
    private String status = "BALANCED";

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TillClosingLogEntity() {}

    public TillClosingLogEntity(UUID reconciliationId, UUID tillId, UUID tellerUserId,
                                String closingMode, BigDecimal electronicBalance,
                                BigDecimal physicalTotal, BigDecimal variance,
                                String status, UUID journalEntryId) {
        this.reconciliationId = reconciliationId;
        this.tillId = tillId;
        this.tellerUserId = tellerUserId;
        this.closingMode = closingMode != null ? closingMode : "BLIND";
        this.electronicBalance = electronicBalance;
        this.physicalTotal = physicalTotal;
        this.variance = variance != null ? variance : BigDecimal.ZERO;
        this.status = status != null ? status : "BALANCED";
        this.journalEntryId = journalEntryId;
        this.createdAt = Instant.now();
    }

    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public UUID getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(UUID reconciliationId) { this.reconciliationId = reconciliationId; }

    public UUID getTillId() { return tillId; }
    public void setTillId(UUID tillId) { this.tillId = tillId; }

    public UUID getTellerUserId() { return tellerUserId; }
    public void setTellerUserId(UUID tellerUserId) { this.tellerUserId = tellerUserId; }

    public String getClosingMode() { return closingMode; }
    public void setClosingMode(String closingMode) { this.closingMode = closingMode; }

    public BigDecimal getElectronicBalance() { return electronicBalance; }
    public void setElectronicBalance(BigDecimal electronicBalance) { this.electronicBalance = electronicBalance; }

    public BigDecimal getPhysicalTotal() { return physicalTotal; }
    public void setPhysicalTotal(BigDecimal physicalTotal) { this.physicalTotal = physicalTotal; }

    public BigDecimal getVariance() { return variance; }
    public void setVariance(BigDecimal variance) { this.variance = variance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(UUID journalEntryId) { this.journalEntryId = journalEntryId; }

    public Instant getCreatedAt() { return createdAt; }
}
