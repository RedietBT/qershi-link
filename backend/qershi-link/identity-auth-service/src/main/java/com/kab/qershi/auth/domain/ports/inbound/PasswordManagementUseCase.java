package com.kab.qershi.auth.domain.ports.inbound;

/**
 * Inbound port for managing and rotating user PIN credentials.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface PasswordManagementUseCase {

    void changePassword(String msisdn, String oldPin, String newPin);
}
