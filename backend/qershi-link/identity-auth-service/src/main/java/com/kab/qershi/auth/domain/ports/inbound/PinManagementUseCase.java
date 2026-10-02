package com.kab.qershi.auth.domain.ports.inbound;

import java.util.UUID;

/**
 * Inbound port for triggering initial login PIN generations and SMS dispatches.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface PinManagementUseCase {

    String resendPinByMsisdn(String msisdn);

    String resendPinByUserId(UUID userId);
}
