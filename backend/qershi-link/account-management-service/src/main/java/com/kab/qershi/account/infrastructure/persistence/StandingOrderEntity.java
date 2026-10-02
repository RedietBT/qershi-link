package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the standing_orders table.
 * Infrastructure layer only — no domain logic.
 *
 * @author KAB Digital Solution PLC
 */
@Entity
@Table(name = "standing_orders")
public class StandingOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "standing_order_no", nullable = false, unique = true, length = 50)
    private String standingOrderNo;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "source_account_no", nullable = false, length = 50)
    private String sourceAccountNo;

    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @Column(name = "target_account_no", nullable = false, length = 50)
    private String targetAccountNo;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "frequency", nullable = false, length = 20)
    private String frequency = "MONTHLY";

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "day_of_week", length = 15)
    private String dayOfWeek;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_executions_count", nullable = false)
    private Integer totalExecutionsCount = 0;

    @Column(name = "failed_attempts_count", nullable = false)
    private Integer failedAttemptsCount = 0;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = OffsetDateTime.now(); }

    public StandingOrderEntity() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getStandingOrderNo() { return standingOrderNo; }
    public void setStandingOrderNo(String standingOrderNo) { this.standingOrderNo = standingOrderNo; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(UUID v) { this.sourceAccountId = v; }
    public String getSourceAccountNo() { return sourceAccountNo; }
    public void setSourceAccountNo(String v) { this.sourceAccountNo = v; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(UUID v) { this.targetAccountId = v; }
    public String getTargetAccountNo() { return targetAccountNo; }
    public void setTargetAccountNo(String v) { this.targetAccountNo = v; }
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getTotalExecutionsCount() { return totalExecutionsCount; }
    public void setTotalExecutionsCount(Integer v) { this.totalExecutionsCount = v; }
    public Integer getFailedAttemptsCount() { return failedAttemptsCount; }
    public void setFailedAttemptsCount(Integer v) { this.failedAttemptsCount = v; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
