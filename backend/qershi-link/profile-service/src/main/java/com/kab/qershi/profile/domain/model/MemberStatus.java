package com.kab.qershi.profile.domain.model;

/**
 * Operational lifecycle states tracking SACCO member account eligibility and governance milestones.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public enum MemberStatus {
    DRAFT,
    PENDING_APPROVAL,
    PENDING_SHARE,
    ACTIVE,
    SUSPENDED,
    DECEASED,
    CLOSED
}
