package com.kab.qershi.auth.domain.model;

/**
 * Account lifecycle validation states enforcing onboarding governance and core banking risk criteria.
 * Separates initial supervisor sign-offs from monetary buy-in milestones.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public enum UserStatus {
    PENDING,

    // 🛠️ FIXED: Added to support the baseline registration state in User.java constructor
    PENDING_APPROVAL,

    // 🛠️ FIXED: Added to support the buy-in milestone state check before activation
    PENDING_SHARE,
    PASSWORD_CHANGE_REQUIRED,

    ACTIVE,
    BLOCKED,
    DEACTIVATED
}