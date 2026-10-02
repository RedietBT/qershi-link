package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.StandingOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case Port — Automated Recurring Standing Orders (Sweep Instructions).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface StandingOrderUseCase {

    record CreateStandingOrderRequest(
            UUID memberId,
            String sourceAccountNo,
            String targetAccountNo,
            BigDecimal amount,
            String frequency,
            Integer dayOfMonth,
            String dayOfWeek,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {}

    record SweepRunResult(
            LocalDate runDate,
            int totalProcessed,
            int successfulSweeps,
            int failedSweeps,
            BigDecimal totalAmountSwept
    ) {}

    StandingOrder create(CreateStandingOrderRequest request);
    StandingOrder pause(UUID standingOrderId);
    StandingOrder resume(UUID standingOrderId);
    StandingOrder cancel(UUID standingOrderId);

    StandingOrder getById(UUID standingOrderId);
    List<StandingOrder> getByMemberId(UUID memberId);
    /** Lookup by member phone number — resolves member's savings account first */
    List<StandingOrder> getByPhoneNumber(String phoneNumber);
    List<StandingOrder> getAll();

    /** Daily EOD hook — executes all due ACTIVE standing orders */
    SweepRunResult runDailySweeps(LocalDate runDate);
}
