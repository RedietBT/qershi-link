package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.common.event.LoanDisbursedEvent;
import com.kab.qershi.common.event.RepaymentReceivedEvent;

/**
 * Outbound port for publishing loan domain events to external event buses.
 * Follows strict Hexagonal Architecture DDD boundaries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanEventPublisherPort {

    /**
     * Publishes LoanDisbursedEvent asynchronously.
     */
    void publishLoanDisbursed(LoanDisbursedEvent event);

    /**
     * Publishes RepaymentReceivedEvent asynchronously.
     */
    void publishRepaymentReceived(RepaymentReceivedEvent event);
}
