package com.kab.qershi.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping tenant-schema till_cash_reconciliations table.
 * Records the physical banknote breakdown and cash variance upon teller till closure.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "till_cash_reconciliations")
public class TillCashReconciliationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reconciliation_id", nullable = false, updatable = false)
    private UUID reconciliationId;

    @Column(name = "till_id", nullable = false)
    private UUID tillId;

    @Column(name = "teller_user_id", nullable = false)
    private UUID tellerUserId;

    @Column(name = "electronic_cash_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal electronicCashBalance;

    @Column(name = "physical_cash_counted", nullable = false, precision = 19, scale = 4)
    private BigDecimal physicalCashCounted;

    @Column(name = "cash_variance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cashVariance = BigDecimal.ZERO;

    @Column(name = "notes_200_count", nullable = false)
    private int notes200Count = 0;

    @Column(name = "notes_100_count", nullable = false)
    private int notes100Count = 0;

    @Column(name = "notes_50_count", nullable = false)
    private int notes50Count = 0;

    @Column(name = "notes_10_count", nullable = false)
    private int notes10Count = 0;

    @Column(name = "notes_5_count", nullable = false)
    private int notes5Count = 0;

    @Column(name = "reconciliation_notes")
    private String reconciliationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (cashVariance == null && electronicCashBalance != null && physicalCashCounted != null) {
            cashVariance = physicalCashCounted.subtract(electronicCashBalance);
        }
    }

    public TillCashReconciliationEntity() {}

    public TillCashReconciliationEntity(UUID tillId, UUID tellerUserId, BigDecimal electronicCashBalance,
                                        BigDecimal physicalCashCounted, BigDecimal cashVariance,
                                        int notes200Count, int notes100Count, int notes50Count,
                                        int notes10Count, int notes5Count, String reconciliationNotes) {
        this.tillId = tillId;
        this.tellerUserId = tellerUserId;
        this.electronicCashBalance = electronicCashBalance;
        this.physicalCashCounted = physicalCashCounted;
        this.cashVariance = cashVariance != null ? cashVariance : physicalCashCounted.subtract(electronicCashBalance);
        this.notes200Count = notes200Count;
        this.notes100Count = notes100Count;
        this.notes50Count = notes50Count;
        this.notes10Count = notes10Count;
        this.notes5Count = notes5Count;
        this.reconciliationNotes = reconciliationNotes;
    }

    public UUID getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(UUID reconciliationId) { this.reconciliationId = reconciliationId; }

    public UUID getTillId() { return tillId; }
    public void setTillId(UUID tillId) { this.tillId = tillId; }

    public UUID getTellerUserId() { return tellerUserId; }
    public void setTellerUserId(UUID tellerUserId) { this.tellerUserId = tellerUserId; }

    public BigDecimal getElectronicCashBalance() { return electronicCashBalance; }
    public void setElectronicCashBalance(BigDecimal electronicCashBalance) { this.electronicCashBalance = electronicCashBalance; }

    public BigDecimal getPhysicalCashCounted() { return physicalCashCounted; }
    public void setPhysicalCashCounted(BigDecimal physicalCashCounted) { this.physicalCashCounted = physicalCashCounted; }

    public BigDecimal getCashVariance() { return cashVariance; }
    public void setCashVariance(BigDecimal cashVariance) { this.cashVariance = cashVariance; }

    public int getNotes200Count() { return notes200Count; }
    public void setNotes200Count(int notes200Count) { this.notes200Count = notes200Count; }

    public int getNotes100Count() { return notes100Count; }
    public void setNotes100Count(int notes100Count) { this.notes100Count = notes100Count; }

    public int getNotes50Count() { return notes50Count; }
    public void setNotes50Count(int notes50Count) { this.notes50Count = notes50Count; }

    public int getNotes10Count() { return notes10Count; }
    public void setNotes10Count(int notes10Count) { this.notes10Count = notes10Count; }

    public int getNotes5Count() { return notes5Count; }
    public void setNotes5Count(int notes5Count) { this.notes5Count = notes5Count; }

    public String getReconciliationNotes() { return reconciliationNotes; }
    public void setReconciliationNotes(String reconciliationNotes) { this.reconciliationNotes = reconciliationNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
