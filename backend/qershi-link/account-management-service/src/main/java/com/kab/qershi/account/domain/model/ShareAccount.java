package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model representing a Member's Share Capital Account under GL 3100.
 * Framework-independent and encapsulates business invariants.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ShareAccount {

    public static final int MANDATORY_MINIMUM_SHARES = 5;
    public static final BigDecimal DEFAULT_NOMINAL_VALUE = new BigDecimal("1000.0000");

    private UUID id;
    private UUID memberId;
    private String accountNumber;
    private int totalShares;
    private BigDecimal shareNominalValue;
    private BigDecimal totalAmount;
    private String status;
    private String saccoCode;
    private String branchCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ShareAccount(UUID id, UUID memberId, String accountNumber, int totalShares,
                        BigDecimal shareNominalValue, BigDecimal totalAmount, String status,
                        String saccoCode, String branchCode, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.accountNumber = accountNumber;
        this.totalShares = totalShares;
        this.shareNominalValue = shareNominalValue != null ? shareNominalValue : DEFAULT_NOMINAL_VALUE;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.status = status != null ? status : "ACTIVE";
        this.saccoCode = saccoCode;
        this.branchCode = branchCode;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public static ShareAccount createNew(UUID memberId, String accountNumber, BigDecimal nominalValue, String saccoCode, String branchCode) {
        return new ShareAccount(
                UUID.randomUUID(),
                memberId,
                accountNumber,
                0,
                nominalValue != null ? nominalValue : DEFAULT_NOMINAL_VALUE,
                BigDecimal.ZERO,
                "ACTIVE",
                saccoCode,
                branchCode,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }

    // ── Domain Business Logic ────────────────────────────────────────────────

    public boolean isMandatoryRequirementMet() {
        return this.totalShares >= MANDATORY_MINIMUM_SHARES;
    }

    public void creditShares(int count, BigDecimal cost) {
        if (count <= 0) {
            throw new IllegalArgumentException("Credit share count must be greater than zero.");
        }
        this.totalShares += count;
        this.totalAmount = this.totalAmount.add(cost);
        this.updatedAt = OffsetDateTime.now();
    }

    public void debitShares(int count, BigDecimal value) {
        if (count <= 0) {
            throw new IllegalArgumentException("Debit share count must be greater than zero.");
        }
        if (this.totalShares < count) {
            throw new IllegalStateException(String.format("Cannot debit %d shares. Account only holds %d.", count, this.totalShares));
        }
        this.totalShares -= count;
        this.totalAmount = this.totalAmount.subtract(value);
        if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAmount = BigDecimal.ZERO;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public String getAccountNumber() { return accountNumber; }
    public int getTotalShares() { return totalShares; }
    public BigDecimal getShareNominalValue() { return shareNominalValue; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getSaccoCode() { return saccoCode; }
    public String getBranchCode() { return branchCode; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
