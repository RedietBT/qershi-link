package com.kab.qershi.transaction.domain.model;

/**
 * Categorization of financial transaction operations in Qershi Link SACCO Core.
 */
public enum TransactionType {
    CASH_DEPOSIT,
    CASH_WITHDRAWAL,
    MEMBER_TRANSFER,
    SYSTEM_FEE,
    INTEREST_PAYOUT,
    REVERSAL
}
