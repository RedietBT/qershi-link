package com.kab.qershi.auth.domain.model;

/**
 * Operational lifecycle states tracking tenant multi-tenant workspace readiness.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public enum SaccoStatus {
    // 🛠️ FIXED: Added to match the structural DB schema allocation phase in Sacco.java
    PENDING_SETUP,
    PENDING,
    ACTIVE,
    SUSPENDED,
    INACTIVE
}