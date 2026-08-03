package com.kab.qershi.account.domain.model;

/**
 * Lifecycle statuses for SACCO member core accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum AccountStatus {
    PENDING_APPROVAL,
    ACTIVE,
    DORMANT,
    FROZEN,
    CLOSED;

    public boolean canDeposit() {
        return this == ACTIVE;
    }

    public boolean canWithdraw() {
        return this == ACTIVE;
    }
}
