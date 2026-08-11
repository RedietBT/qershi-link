package com.kab.qershi.auth.infrastructure.security;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates PIN complexity and security rules according to Core Banking Standards.
 * Rejects trivial, sequential, repeating, or phone-number-derived PINs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class PinValidator {

    private static final List<String> TRIVIAL_PINS = List.of(
            "000000", "111111", "222222", "333333", "444444",
            "555555", "666666", "777777", "888888", "999999",
            "123456", "654321", "012345", "543210", "123123",
            "112233", "121212", "654321"
    );

    /**
     * Validates PIN complexity against Central Bank and Tier-1 Core Banking standards.
     *
     * @param newPin  The proposed new PIN.
     * @param msisdn  The user's phone number.
     * @param oldPin  The current/previous PIN (optional).
     * @throws IllegalArgumentException if the proposed PIN fails security rules.
     */
    public void validatePin(String newPin, String msisdn, String oldPin) {
        if (newPin == null || newPin.length() != 6 || !newPin.matches("^\\d{6}$")) {
            throw new IllegalArgumentException("PIN must be exactly 6 numeric digits.");
        }

        if (oldPin != null && oldPin.equals(newPin)) {
            throw new IllegalArgumentException("New PIN cannot be identical to the current PIN.");
        }

        if (TRIVIAL_PINS.contains(newPin)) {
            throw new IllegalArgumentException("PIN is too simple or predictable (e.g. 123456 or 111111). Please choose a more secure PIN.");
        }

        if (isSequential(newPin)) {
            throw new IllegalArgumentException("PIN cannot contain sequential digits.");
        }

        if (msisdn != null && !msisdn.isBlank()) {
            String digitsOnlyPhone = msisdn.replaceAll("\\D", "");
            if (digitsOnlyPhone.contains(newPin)) {
                throw new IllegalArgumentException("PIN cannot be derived from your phone number.");
            }
        }
    }

    private boolean isSequential(String pin) {
        boolean ascending = true;
        boolean descending = true;
        for (int i = 0; i < pin.length() - 1; i++) {
            int current = Character.getNumericValue(pin.charAt(i));
            int next = Character.getNumericValue(pin.charAt(i + 1));
            if (next != current + 1) ascending = false;
            if (next != current - 1) descending = false;
        }
        return ascending || descending;
    }
}
