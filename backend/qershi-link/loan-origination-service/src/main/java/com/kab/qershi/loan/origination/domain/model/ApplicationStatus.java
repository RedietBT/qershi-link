package com.kab.qershi.loan.origination.domain.model;

/**
 * State lifecycle enum for SACCO loan applications.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    DISBURSED,
    REJECTED,
    REJECTED_ELIGIBILITY
}
