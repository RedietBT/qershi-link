package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.common.event.AccountOpenedEvent;

/**
 * Outbound port for publishing account domain events to external event buses.
 * Follows strict Hexagonal Architecture DDD boundaries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountEventPublisherPort {

    /**
     * Publishes AccountOpenedEvent asynchronously.
     */
    void publishAccountOpened(AccountOpenedEvent event);
}
