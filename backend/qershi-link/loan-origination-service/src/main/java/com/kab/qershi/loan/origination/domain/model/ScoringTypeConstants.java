package com.kab.qershi.loan.origination.domain.model;

/**
 * Standard default credit scoring strategy constants.
 * SACCOs can also define custom scoring strategy codes dynamically via configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class ScoringTypeConstants {

    private ScoringTypeConstants() {}

    public static final String COLLATERAL = "COLLATERAL";
    public static final String SAVINGS = "SAVINGS";
    public static final String AGRI_PRODUCTIVITY = "AGRI_PRODUCTIVITY";
}
