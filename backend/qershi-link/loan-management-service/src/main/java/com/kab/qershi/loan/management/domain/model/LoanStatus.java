package com.kab.qershi.loan.management.domain.model;

/**
 * Status of a disbursed loan account in the SACCO Core Banking system.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum LoanStatus {
    PENDING_DISBURSEMENT,
    DISBURSED,
    ACTIVE,
    CLOSED,
    DEFAULTED,
    RESTRUCTURED
}
