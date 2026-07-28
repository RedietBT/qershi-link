package com.kab.qershi.profile.infrastructure.exception;

/**
 * Domain Exception thrown when attempting to register duplicate profile data (e.g. existing user ID or member number).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
