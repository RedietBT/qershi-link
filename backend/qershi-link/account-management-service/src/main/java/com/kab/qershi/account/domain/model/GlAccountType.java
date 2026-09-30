package com.kab.qershi.account.domain.model;

/**
 * Standard 5 pillars of the Financial Accounting General Ledger (FLEXCUBE / Temenos / Mifos standard).
 * Determines normal debit/credit balances, balance sheet placement, and profit/loss calculation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum GlAccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE;

    /**
     * In double-entry bookkeeping, Assets and Expenses naturally carry DEBIT balances.
     */
    public boolean isDebitNormal() {
        return this == ASSET || this == EXPENSE;
    }

    /**
     * In double-entry bookkeeping, Liabilities, Equity, and Revenue naturally carry CREDIT balances.
     */
    public boolean isCreditNormal() {
        return this == LIABILITY || this == EQUITY || this == REVENUE;
    }
}
