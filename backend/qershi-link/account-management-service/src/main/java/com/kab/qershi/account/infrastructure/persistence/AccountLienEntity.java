package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.LienStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping account_liens table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "account_liens")
public class AccountLienEntity {

    @Id
    @Column(name = "lien_id")
    private UUID lienId;

    @Column(name = "account_no", nullable = false, length = 50)
    private String accountNo;

    @Column(name = "lien_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal lienAmount;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "placed_by_user_id", nullable = false)
    private UUID placedByUserId;

    @Column(name = "released_by_user_id")
    private UUID releasedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LienStatus status;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    public AccountLienEntity() {}

    public AccountLienEntity(UUID lienId, String accountNo, BigDecimal lienAmount, String reason,
                             String referenceNo, UUID placedByUserId, UUID releasedByUserId,
                             LienStatus status, LocalDateTime placedAt, LocalDateTime releasedAt) {
        this.lienId = lienId;
        this.accountNo = accountNo;
        this.lienAmount = lienAmount;
        this.reason = reason;
        this.referenceNo = referenceNo;
        this.placedByUserId = placedByUserId;
        this.releasedByUserId = releasedByUserId;
        this.status = status;
        this.placedAt = placedAt;
        this.releasedAt = releasedAt;
    }

    // Getters and Setters
    public UUID getLienId() { return lienId; }
    public void setLienId(UUID lienId) { this.lienId = lienId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public BigDecimal getLienAmount() { return lienAmount; }
    public void setLienAmount(BigDecimal lienAmount) { this.lienAmount = lienAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public UUID getPlacedByUserId() { return placedByUserId; }
    public void setPlacedByUserId(UUID placedByUserId) { this.placedByUserId = placedByUserId; }

    public UUID getReleasedByUserId() { return releasedByUserId; }
    public void setReleasedByUserId(UUID releasedByUserId) { this.releasedByUserId = releasedByUserId; }

    public LienStatus getStatus() { return status; }
    public void setStatus(LienStatus status) { this.status = status; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
