package com.kab.qershi.account.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing the Core Banking System Business Date.
 * Controls daytime transaction posting locks and EOD batch progression.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SystemBusinessDate {

    private UUID id;
    private LocalDate currentBusinessDate;
    private String status; // 'OPEN', 'CUTOFF_LOCKED', 'PROCESSING_EOD', 'CLOSED'
    private Boolean isMonthEnd;
    private LocalDateTime lastEodCompletedAt;
    private UUID updatedByUserId;
    private LocalDateTime updatedAt;

    public SystemBusinessDate() {
        this.currentBusinessDate = LocalDate.now();
        this.status = "OPEN";
        this.isMonthEnd = false;
        this.updatedAt = LocalDateTime.now();
    }

    public SystemBusinessDate(UUID id, LocalDate currentBusinessDate, String status, Boolean isMonthEnd,
                              LocalDateTime lastEodCompletedAt, UUID updatedByUserId, LocalDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.currentBusinessDate = currentBusinessDate != null ? currentBusinessDate : LocalDate.now();
        this.status = status != null ? status : "OPEN";
        this.isMonthEnd = isMonthEnd != null ? isMonthEnd : false;
        this.lastEodCompletedAt = lastEodCompletedAt;
        this.updatedByUserId = updatedByUserId;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public void lockForCutoff() {
        this.status = "CUTOFF_LOCKED";
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessingEod() {
        this.status = "PROCESSING_EOD";
        this.updatedAt = LocalDateTime.now();
    }

    public void rolloverDate(LocalDate nextDate, boolean isNextMonthEnd) {
        this.currentBusinessDate = nextDate;
        this.isMonthEnd = isNextMonthEnd;
        this.status = "OPEN";
        this.lastEodCompletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void unlock() {
        this.status = "OPEN";
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(this.status);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getCurrentBusinessDate() { return currentBusinessDate; }
    public void setCurrentBusinessDate(LocalDate currentBusinessDate) { this.currentBusinessDate = currentBusinessDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsMonthEnd() { return isMonthEnd; }
    public void setIsMonthEnd(Boolean isMonthEnd) { this.isMonthEnd = isMonthEnd; }

    public LocalDateTime getLastEodCompletedAt() { return lastEodCompletedAt; }
    public void setLastEodCompletedAt(LocalDateTime lastEodCompletedAt) { this.lastEodCompletedAt = lastEodCompletedAt; }

    public UUID getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(UUID updatedByUserId) { this.updatedByUserId = updatedByUserId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
