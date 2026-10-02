package com.kab.qershi.transaction.domain.ports.outbound;

import com.kab.qershi.common.event.TransactionCompletedEvent;

/**
 * Outbound port for publishing transaction domain events to external event buses.
 * Follows strict Hexagonal Architecture DDD boundaries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransactionEventPublisherPort {

    /**
     * Publishes transaction completed domain event asynchronously.
     */
    void publishTransactionCompleted(TransactionCompletedEvent event);
}
