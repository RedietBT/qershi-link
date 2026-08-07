package com.kab.qershi.loan.origination.domain.model;

/**
 * Standard default collateral type constants.
 * SACCOs can also define custom collateral types dynamically via the collateral_types database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public final class CollateralTypeConstants {

    private CollateralTypeConstants() {}

    public static final String LAND = "LAND";
    public static final String CROP = "CROP";
    public static final String VEHICLE = "VEHICLE";
    public static final String GUARANTOR = "GUARANTOR";
    public static final String GOLD = "GOLD";
    public static final String SALARY_ASSIGNMENT = "SALARY_ASSIGNMENT";
}
