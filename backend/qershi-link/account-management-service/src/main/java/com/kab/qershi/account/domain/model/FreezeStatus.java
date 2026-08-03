package com.kab.qershi.account.domain.model;

/**
 * Administrative freeze control states placed by risk/compliance officers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum FreezeStatus {
    NONE,
    DEBIT_FREEZE,
    CREDIT_FREEZE,
    FULL_FREEZE;

    public boolean blocksDebit() {
        return this == DEBIT_FREEZE || this == FULL_FREEZE;
    }

    public boolean blocksCredit() {
        return this == CREDIT_FREEZE || this == FULL_FREEZE;
    }
}
