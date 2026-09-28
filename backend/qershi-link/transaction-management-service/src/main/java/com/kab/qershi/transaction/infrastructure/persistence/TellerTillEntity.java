package com.kab.qershi.transaction.infrastructure.persistence;

import com.kab.qershi.transaction.domain.model.TillStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping tenant-schema teller_tills database table.
 * Tracks physical cash drawers assigned to tellers, live cash balances, and status.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "teller_tills")
public class TellerTillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "till_id", nullable = false, updatable = false)
    private UUID tillId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode;

    @Column(name = "teller_user_id", nullable = false)
    private UUID tellerUserId;

    @Column(name = "till_name", nullable = false, length = 100)
    private String tillName;

    @Column(name = "till_gl_code", nullable = false, length = 50)
    private String tillGlCode;

    @Column(name = "opening_cash", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingCash = BigDecimal.ZERO;

    @Column(name = "current_cash", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentCash = BigDecimal.ZERO;

    @Column(name = "max_cash_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal maxCashLimit = new BigDecimal("200000.0000");

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TillStatus status = TillStatus.CLOSED;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
        if (openingCash == null) openingCash = BigDecimal.ZERO;
        if (currentCash == null) currentCash = BigDecimal.ZERO;
        if (maxCashLimit == null) maxCashLimit = new BigDecimal("200000.0000");
        if (status == null) status = TillStatus.CLOSED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public TellerTillEntity() {}

    public TellerTillEntity(UUID tillId, UUID branchId, String branchCode, UUID tellerUserId,
                            String tillName, String tillGlCode, BigDecimal openingCash,
                            BigDecimal currentCash, BigDecimal maxCashLimit, TillStatus status) {
        this.tillId = tillId;
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.tellerUserId = tellerUserId;
        this.tillName = tillName;
        this.tillGlCode = tillGlCode;
        this.openingCash = openingCash != null ? openingCash : BigDecimal.ZERO;
        this.currentCash = currentCash != null ? currentCash : BigDecimal.ZERO;
        this.maxCashLimit = maxCashLimit != null ? maxCashLimit : new BigDecimal("200000.0000");
        this.status = status != null ? status : TillStatus.CLOSED;
    }

    public UUID getTillId() { return tillId; }
    public void setTillId(UUID tillId) { this.tillId = tillId; }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public UUID getTellerUserId() { return tellerUserId; }
    public void setTellerUserId(UUID tellerUserId) { this.tellerUserId = tellerUserId; }

    public String getTillName() { return tillName; }
    public void setTillName(String tillName) { this.tillName = tillName; }

    public String getTillGlCode() { return tillGlCode; }
    public void setTillGlCode(String tillGlCode) { this.tillGlCode = tillGlCode; }

    public BigDecimal getOpeningCash() { return openingCash; }
    public void setOpeningCash(BigDecimal openingCash) { this.openingCash = openingCash; }

    public BigDecimal getCurrentCash() { return currentCash; }
    public void setCurrentCash(BigDecimal currentCash) { this.currentCash = currentCash; }

    public BigDecimal getMaxCashLimit() { return maxCashLimit; }
    public void setMaxCashLimit(BigDecimal maxCashLimit) { this.maxCashLimit = maxCashLimit; }

    public TillStatus getStatus() { return status; }
    public void setStatus(TillStatus status) { this.status = status; }

    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
