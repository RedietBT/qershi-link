package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain model representing a physical cash drawer (till) assigned to a teller.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TellerTill {

    private UUID tillId;
    private UUID branchId;
    private String branchCode;
    private UUID tellerUserId;
    private String tillName;
    private String tillGlCode;
    private BigDecimal openingCash;
    private BigDecimal currentCash;
    private BigDecimal maxCashLimit;
    private TillStatus status;
    private Instant openedAt;
    private Instant closedAt;
    private Instant createdAt;

    public TellerTill() {
        this.openingCash = BigDecimal.ZERO;
        this.currentCash = BigDecimal.ZERO;
        this.maxCashLimit = new BigDecimal("200000.00");
        this.status = TillStatus.CLOSED;
        this.createdAt = Instant.now();
    }

    public TellerTill(UUID tillId, UUID branchId, String branchCode, UUID tellerUserId,
                      String tillName, String tillGlCode, BigDecimal openingCash,
                      BigDecimal currentCash, BigDecimal maxCashLimit, TillStatus status,
                      Instant openedAt, Instant closedAt, Instant createdAt) {
        this.tillId = tillId;
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.tellerUserId = tellerUserId;
        this.tillName = tillName;
        this.tillGlCode = tillGlCode;
        this.openingCash = openingCash != null ? openingCash : BigDecimal.ZERO;
        this.currentCash = currentCash != null ? currentCash : BigDecimal.ZERO;
        this.maxCashLimit = maxCashLimit != null ? maxCashLimit : new BigDecimal("200000.00");
        this.status = status != null ? status : TillStatus.CLOSED;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getTillId() {
        return tillId;
    }

    public void setTillId(UUID tillId) {
        this.tillId = tillId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public UUID getTellerUserId() {
        return tellerUserId;
    }

    public void setTellerUserId(UUID tellerUserId) {
        this.tellerUserId = tellerUserId;
    }

    public String getTillName() {
        return tillName;
    }

    public void setTillName(String tillName) {
        this.tillName = tillName;
    }

    public String getTillGlCode() {
        return tillGlCode;
    }

    public void setTillGlCode(String tillGlCode) {
        this.tillGlCode = tillGlCode;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public BigDecimal getCurrentCash() {
        return currentCash;
    }

    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
    }

    public BigDecimal getMaxCashLimit() {
        return maxCashLimit;
    }

    public void setMaxCashLimit(BigDecimal maxCashLimit) {
        this.maxCashLimit = maxCashLimit;
    }

    public TillStatus getStatus() {
        return status;
    }

    public void setStatus(TillStatus status) {
        this.status = status;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
