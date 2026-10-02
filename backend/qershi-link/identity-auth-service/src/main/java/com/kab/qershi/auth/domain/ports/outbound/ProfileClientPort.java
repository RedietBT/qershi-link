package com.kab.qershi.auth.domain.ports.outbound;

import java.util.UUID;

/**
 * Outbound port for communicating with downstream Member Profile Service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProfileClientPort {

    void triggerProfileCascadeDeletion(UUID userId);
}
