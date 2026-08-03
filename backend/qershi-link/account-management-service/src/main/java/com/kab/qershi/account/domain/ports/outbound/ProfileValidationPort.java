package com.kab.qershi.account.domain.ports.outbound;

import java.util.UUID;

/**
 * Outbound port for inter-service validation of member profile status via gRPC.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProfileValidationPort {

    boolean isMemberActive(UUID userId);
}
