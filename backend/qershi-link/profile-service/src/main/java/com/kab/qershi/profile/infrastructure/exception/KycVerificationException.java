package com.kab.qershi.profile.infrastructure.exception;

/**
 * Domain Exception thrown when KYC document submission or verification fails rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class KycVerificationException extends RuntimeException {

    public KycVerificationException(String message) {
        super(message);
    }

    public KycVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
