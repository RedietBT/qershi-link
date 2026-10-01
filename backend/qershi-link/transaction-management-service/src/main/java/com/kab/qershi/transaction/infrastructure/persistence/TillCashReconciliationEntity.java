package com.kab.qershi.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping tenant-schema till_cash_reconciliations table.
 * Records the physical banknote breakdown, cash variance, GL adjustment link,
 * and supervisor authorization status upon teller till closure.
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

    @Column(name = "coins_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal coinsAmount = BigDecimal.ZERO;

    @Column(name = "variance_type", nullable = false, length = 20)
    private String varianceType = "NONE"; // 'NONE', 'SHORTAGE', 'OVERAGE'

    @Column(name = "variance_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal varianceAmount = BigDecimal.ZERO;

    @Column(name = "variance_gl_code", length = 50)
    private String varianceGlCode;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @Column(name = "status", nullable = false, length = 40)
    private String status = "BALANCED"; // 'BALANCED', 'PENDING_SUPERVISOR_APPROVAL', 'SUPERVISOR_APPROVED'

    @Column(name = "supervisor_approved_by")
    private UUID supervisorApprovedBy;

    @Column(name = "supervisor_approved_at")
    private Instant supervisorApprovedAt;

    @Column(name = "supervisor_notes", columnDefinition = "TEXT")
    private String supervisorNotes;

    @Column(name = "reconciliation_notes", columnDefinition = "TEXT")
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
        this(tillId, tellerUserId, electronicCashBalance, physicalCashCounted, cashVariance,
                notes200Count, notes100Count, notes50Count, notes10Count, notes5Count,
                BigDecimal.ZERO, "NONE", BigDecimal.ZERO, null, null, "BALANCED", reconciliationNotes);
    }

    public TillCashReconciliationEntity(UUID tillId, UUID tellerUserId, BigDecimal electronicCashBalance,
                                        BigDecimal physicalCashCounted, BigDecimal cashVariance,
                                        int notes200Count, int notes100Count, int notes50Count,
                                        int notes10Count, int notes5Count, BigDecimal coinsAmount,
                                        String varianceType, BigDecimal varianceAmount, String varianceGlCode,
                                        UUID journalEntryId, String status, String reconciliationNotes) {
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
        this.coinsAmount = coinsAmount != null ? coinsAmount : BigDecimal.ZERO;
        this.varianceType = varianceType != null ? varianceType : "NONE";
        this.varianceAmount = varianceAmount != null ? varianceAmount : BigDecimal.ZERO;
        this.varianceGlCode = varianceGlCode;
        this.journalEntryId = journalEntryId;
        this.status = status != null ? status : "BALANCED";
        this.reconciliationNotes = reconciliationNotes;
        this.createdAt = Instant.now();
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

    public BigDecimal getCoinsAmount() { return coinsAmount; }
    public void setCoinsAmount(BigDecimal coinsAmount) { this.coinsAmount = coinsAmount; }

    public String getVarianceType() { return varianceType; }
    public void setVarianceType(String varianceType) { this.varianceType = varianceType; }

    public BigDecimal getVarianceAmount() { return varianceAmount; }
    public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }

    public String getVarianceGlCode() { return varianceGlCode; }
    public void setVarianceGlCode(String varianceGlCode) { this.varianceGlCode = varianceGlCode; }

    public UUID getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(UUID journalEntryId) { this.journalEntryId = journalEntryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getSupervisorApprovedBy() { return supervisorApprovedBy; }
    public void setSupervisorApprovedBy(UUID supervisorApprovedBy) { this.supervisorApprovedBy = supervisorApprovedBy; }

    public Instant getSupervisorApprovedAt() { return supervisorApprovedAt; }
    public void setSupervisorApprovedAt(Instant supervisorApprovedAt) { this.supervisorApprovedAt = supervisorApprovedAt; }

    public String getSupervisorNotes() { return supervisorNotes; }
    public void setSupervisorNotes(String supervisorNotes) { this.supervisorNotes = supervisorNotes; }

    public String getReconciliationNotes() { return reconciliationNotes; }
    public void setReconciliationNotes(String reconciliationNotes) { this.reconciliationNotes = reconciliationNotes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
