package com.kab.qershi.loan.origination.domain.model;

/**
 * Maker-Checker decision action type for loan approval audit trails.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum WorkflowAction {
    SUBMIT,
    VERIFY,
    APPROVE,
    REJECT
}
