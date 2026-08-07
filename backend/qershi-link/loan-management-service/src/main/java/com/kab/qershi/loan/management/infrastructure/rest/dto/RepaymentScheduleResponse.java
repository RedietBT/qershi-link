package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.model.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Response DTO for an Amortization Schedule Installment.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record RepaymentScheduleResponse(
        UUID scheduleId,
        UUID accountId,
        Integer installmentNo,
        LocalDate dueDate,
        BigDecimal principalDue,
        BigDecimal interestDue,
        BigDecimal totalDue,
        BigDecimal amountPaid,
        ScheduleStatus status
) {
    public static RepaymentScheduleResponse fromDomain(RepaymentSchedule domain) {
        if (domain == null) return null;
        return new RepaymentScheduleResponse(
                domain.getScheduleId(),
                domain.getAccountId(),
                domain.getInstallmentNo(),
                domain.getDueDate(),
                domain.getPrincipalDue(),
                domain.getInterestDue(),
                domain.getTotalDue(),
                domain.getAmountPaid(),
                domain.getStatus()
        );
    }
}
