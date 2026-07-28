package com.kab.qershi.profile.infrastructure.exception;

/**
 * Domain Exception thrown when a member profile, address, or record cannot be found.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
