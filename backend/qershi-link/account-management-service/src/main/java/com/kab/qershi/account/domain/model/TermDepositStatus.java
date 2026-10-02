package com.kab.qershi.account.domain.model;

/**
 * Domain Enum representing the lifecycle status of a Fixed Term Deposit contract.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum TermDepositStatus {
    ACTIVE,
    MATURED,
    ROLLED_OVER,
    CLOSED_NORMAL,
    CLOSED_EARLY,
    PENDING_APPROVAL
}
