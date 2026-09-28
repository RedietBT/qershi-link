package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping system_business_date table.
 * Controls the active core banking financial business date and daytime posting locks.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "system_business_date")
public class SystemBusinessDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "current_business_date", nullable = false)
    private LocalDate currentBusinessDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // 'OPEN', 'CUTOFF_LOCKED', 'PROCESSING_EOD', 'CLOSED'

    @Column(name = "is_month_end", nullable = false)
    private Boolean isMonthEnd = false;

    @Column(name = "last_eod_completed_at")
    private LocalDateTime lastEodCompletedAt;

    @Column(name = "updated_by_user_id")
    private UUID updatedByUserId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SystemBusinessDateEntity() {}

    public SystemBusinessDateEntity(UUID id, LocalDate currentBusinessDate, String status, Boolean isMonthEnd,
                                    LocalDateTime lastEodCompletedAt, UUID updatedByUserId, LocalDateTime updatedAt) {
        this.id = id;
        this.currentBusinessDate = currentBusinessDate;
        this.status = status;
        this.isMonthEnd = isMonthEnd;
        this.lastEodCompletedAt = lastEodCompletedAt;
        this.updatedByUserId = updatedByUserId;
        this.updatedAt = updatedAt;
    }

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
