package com.kab.qershi.profile.infrastructure.exception;

/**
 * Domain Exception thrown when total payout allocation across active Next of Kin beneficiaries exceeds 100.00%.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class InvalidBeneficiaryAllocationException extends RuntimeException {

    public InvalidBeneficiaryAllocationException(String message) {
        super(message);
    }

    public InvalidBeneficiaryAllocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
