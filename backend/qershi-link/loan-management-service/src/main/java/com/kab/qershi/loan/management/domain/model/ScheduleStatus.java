package com.kab.qershi.loan.management.domain.model;

/**
 * Payment status for an individual installment in an amortization schedule.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum ScheduleStatus {
    PENDING,
    PAID,
    PARTIALLY_PAID,
    OVERDUE
}
