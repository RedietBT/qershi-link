package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain model representing the end-of-shift Blind Till Balancing and Cash Reconciliation record.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TillCashReconciliation {

    private UUID reconciliationId;
    private UUID tillId;
    private UUID tellerUserId;
    private BigDecimal electronicCashBalance;
    private BigDecimal physicalCashCounted;
    private BigDecimal cashVariance;
    private int notes200Count;
    private int notes100Count;
    private int notes50Count;
    private int notes10Count;
    private int notes5Count;
    private BigDecimal coinsAmount;
    private String varianceType;
    private BigDecimal varianceAmount;
    private String varianceGlCode;
    private UUID journalEntryId;
    private String status;
    private UUID supervisorApprovedBy;
    private Instant supervisorApprovedAt;
    private String supervisorNotes;
    private String reconciliationNotes;
    private Instant createdAt;

    public TillCashReconciliation() {
        this.cashVariance = BigDecimal.ZERO;
        this.coinsAmount = BigDecimal.ZERO;
        this.varianceType = "NONE";
        this.varianceAmount = BigDecimal.ZERO;
        this.status = "BALANCED";
        this.createdAt = Instant.now();
    }

    public TillCashReconciliation(UUID reconciliationId, UUID tillId, UUID tellerUserId,
                                  BigDecimal electronicCashBalance, BigDecimal physicalCashCounted,
                                  BigDecimal cashVariance, int notes200Count, int notes100Count,
                                  int notes50Count, int notes10Count, int notes5Count,
                                  BigDecimal coinsAmount, String varianceType, BigDecimal varianceAmount,
                                  String varianceGlCode, UUID journalEntryId, String status,
                                  UUID supervisorApprovedBy, Instant supervisorApprovedAt,
                                  String supervisorNotes, String reconciliationNotes, Instant createdAt) {
        this.reconciliationId = reconciliationId;
        this.tillId = tillId;
        this.tellerUserId = tellerUserId;
        this.electronicCashBalance = electronicCashBalance;
        this.physicalCashCounted = physicalCashCounted;
        this.cashVariance = cashVariance != null ? cashVariance : BigDecimal.ZERO;
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
        this.supervisorApprovedBy = supervisorApprovedBy;
        this.supervisorApprovedAt = supervisorApprovedAt;
        this.supervisorNotes = supervisorNotes;
        this.reconciliationNotes = reconciliationNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getReconciliationId() {
        return reconciliationId;
    }

    public void setReconciliationId(UUID reconciliationId) {
        this.reconciliationId = reconciliationId;
    }

    public UUID getTillId() {
        return tillId;
    }

    public void setTillId(UUID tillId) {
        this.tillId = tillId;
    }

    public UUID getTellerUserId() {
        return tellerUserId;
    }

    public void setTellerUserId(UUID tellerUserId) {
        this.tellerUserId = tellerUserId;
    }

    public BigDecimal getElectronicCashBalance() {
        return electronicCashBalance;
    }

    public void setElectronicCashBalance(BigDecimal electronicCashBalance) {
        this.electronicCashBalance = electronicCashBalance;
    }

    public BigDecimal getPhysicalCashCounted() {
        return physicalCashCounted;
    }

    public void setPhysicalCashCounted(BigDecimal physicalCashCounted) {
        this.physicalCashCounted = physicalCashCounted;
    }

    public BigDecimal getCashVariance() {
        return cashVariance;
    }

    public void setCashVariance(BigDecimal cashVariance) {
        this.cashVariance = cashVariance;
    }

    public int getNotes200Count() {
        return notes200Count;
    }

    public void setNotes200Count(int notes200Count) {
        this.notes200Count = notes200Count;
    }

    public int getNotes100Count() {
        return notes100Count;
    }

    public void setNotes100Count(int notes100Count) {
        this.notes100Count = notes100Count;
    }

    public int getNotes50Count() {
        return notes50Count;
    }

    public void setNotes50Count(int notes50Count) {
        this.notes50Count = notes50Count;
    }

    public int getNotes10Count() {
        return notes10Count;
    }

    public void setNotes10Count(int notes10Count) {
        this.notes10Count = notes10Count;
    }

    public int getNotes5Count() {
        return notes5Count;
    }

    public void setNotes5Count(int notes5Count) {
        this.notes5Count = notes5Count;
    }

    public BigDecimal getCoinsAmount() {
        return coinsAmount;
    }

    public void setCoinsAmount(BigDecimal coinsAmount) {
        this.coinsAmount = coinsAmount;
    }

    public String getVarianceType() {
        return varianceType;
    }

    public void setVarianceType(String varianceType) {
        this.varianceType = varianceType;
    }

    public BigDecimal getVarianceAmount() {
        return varianceAmount;
    }

    public void setVarianceAmount(BigDecimal varianceAmount) {
        this.varianceAmount = varianceAmount;
    }

    public String getVarianceGlCode() {
        return varianceGlCode;
    }

    public void setVarianceGlCode(String varianceGlCode) {
        this.varianceGlCode = varianceGlCode;
    }

    public UUID getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(UUID journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getSupervisorApprovedBy() {
        return supervisorApprovedBy;
    }

    public void setSupervisorApprovedBy(UUID supervisorApprovedBy) {
        this.supervisorApprovedBy = supervisorApprovedBy;
    }

    public Instant getSupervisorApprovedAt() {
        return supervisorApprovedAt;
    }

    public void setSupervisorApprovedAt(Instant supervisorApprovedAt) {
        this.supervisorApprovedAt = supervisorApprovedAt;
    }

    public String getSupervisorNotes() {
        return supervisorNotes;
    }

    public void setSupervisorNotes(String supervisorNotes) {
        this.supervisorNotes = supervisorNotes;
    }

    public String getReconciliationNotes() {
        return reconciliationNotes;
    }

    public void setReconciliationNotes(String reconciliationNotes) {
        this.reconciliationNotes = reconciliationNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
