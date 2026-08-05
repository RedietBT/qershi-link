package com.kab.qershi.transaction.domain.model;

/**
 * Processing state lifecycle for financial transactions.
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}
