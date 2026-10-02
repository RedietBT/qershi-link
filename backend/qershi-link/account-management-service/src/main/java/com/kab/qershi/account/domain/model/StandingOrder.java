package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model — Automated Recurring Standing Order (Sweep Instruction).
 * Represents a member's instruction to sweep a fixed amount between accounts on a schedule.
 * No JPA / infrastructure coupling.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class StandingOrder {

    // ── Frequency constants ───────────────────────────────────────────────────
    public static final String FREQ_DAILY      = "DAILY";
    public static final String FREQ_WEEKLY     = "WEEKLY";
    public static final String FREQ_BI_WEEKLY  = "BI_WEEKLY";
    public static final String FREQ_MONTHLY    = "MONTHLY";

    // ── Status constants ──────────────────────────────────────────────────────
    public static final String STATUS_ACTIVE    = "ACTIVE";
    public static final String STATUS_PAUSED    = "PAUSED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED    = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private UUID id;
    private String standingOrderNo;
    private UUID sourceAccountId;
    private String sourceAccountNo;
    private UUID targetAccountId;
    private String targetAccountNo;
    private UUID memberId;
    private BigDecimal amount;
    private String frequency;
    private Integer dayOfMonth;
    private String dayOfWeek;
    private LocalDate startDate;
    private LocalDate nextRunDate;
    private LocalDate endDate;
    private int totalExecutionsCount;
    private int failedAttemptsCount;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public StandingOrder() {}

    public StandingOrder(
            UUID id, String standingOrderNo,
            UUID sourceAccountId, String sourceAccountNo,
            UUID targetAccountId, String targetAccountNo,
            UUID memberId, BigDecimal amount, String frequency,
            Integer dayOfMonth, String dayOfWeek,
            LocalDate startDate, LocalDate nextRunDate, LocalDate endDate,
            int totalExecutionsCount, int failedAttemptsCount,
            String description, String status,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.standingOrderNo = standingOrderNo;
        this.sourceAccountId = sourceAccountId;
        this.sourceAccountNo = sourceAccountNo;
        this.targetAccountId = targetAccountId;
        this.targetAccountNo = targetAccountNo;
        this.memberId = memberId;
        this.amount = amount;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.startDate = startDate;
        this.nextRunDate = nextRunDate;
        this.endDate = endDate;
        this.totalExecutionsCount = totalExecutionsCount;
        this.failedAttemptsCount = failedAttemptsCount;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Domain Behaviour ─────────────────────────────────────────────────────

    public boolean isActive()    { return STATUS_ACTIVE.equalsIgnoreCase(status); }
    public boolean isPaused()    { return STATUS_PAUSED.equalsIgnoreCase(status); }
    public boolean isCancelled() { return STATUS_CANCELLED.equalsIgnoreCase(status); }
    public boolean isDueToday()  { return nextRunDate != null && !nextRunDate.isAfter(LocalDate.now()); }

    /** Advances nextRunDate after a successful execution */
    public void advanceNextRunDate() {
        this.totalExecutionsCount++;
        this.updatedAt = OffsetDateTime.now();

        if (endDate != null && nextRunDate.isAfter(endDate)) {
            this.status = STATUS_COMPLETED;
            return;
        }

        switch (frequency.toUpperCase()) {
            case FREQ_DAILY     -> this.nextRunDate = nextRunDate.plusDays(1);
            case FREQ_WEEKLY    -> this.nextRunDate = nextRunDate.plusWeeks(1);
            case FREQ_BI_WEEKLY -> this.nextRunDate = nextRunDate.plusWeeks(2);
            case FREQ_MONTHLY   -> this.nextRunDate = nextRunDate.plusMonths(1);
            default             -> this.nextRunDate = nextRunDate.plusMonths(1);
        }

        if (endDate != null && nextRunDate.isAfter(endDate)) {
            this.status = STATUS_COMPLETED;
        }
    }

    public void recordFailure() {
        this.failedAttemptsCount++;
        this.updatedAt = OffsetDateTime.now();
        if (this.failedAttemptsCount >= 3) {
            this.status = STATUS_FAILED;
        }
    }

    public void pause()  { this.status = STATUS_PAUSED;    this.updatedAt = OffsetDateTime.now(); }
    public void resume() { this.status = STATUS_ACTIVE;    this.updatedAt = OffsetDateTime.now(); }
    public void cancel() { this.status = STATUS_CANCELLED; this.updatedAt = OffsetDateTime.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getStandingOrderNo() { return standingOrderNo; }
    public void setStandingOrderNo(String standingOrderNo) { this.standingOrderNo = standingOrderNo; }

    public UUID getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(UUID sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public String getSourceAccountNo() { return sourceAccountNo; }
    public void setSourceAccountNo(String sourceAccountNo) { this.sourceAccountNo = sourceAccountNo; }

    public UUID getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(UUID targetAccountId) { this.targetAccountId = targetAccountId; }

    public String getTargetAccountNo() { return targetAccountNo; }
    public void setTargetAccountNo(String targetAccountNo) { this.targetAccountNo = targetAccountNo; }

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

    public int getTotalExecutionsCount() { return totalExecutionsCount; }
    public void setTotalExecutionsCount(int totalExecutionsCount) { this.totalExecutionsCount = totalExecutionsCount; }

    public int getFailedAttemptsCount() { return failedAttemptsCount; }
    public void setFailedAttemptsCount(int failedAttemptsCount) { this.failedAttemptsCount = failedAttemptsCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
