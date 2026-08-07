package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Repository Port for RepaymentSchedule persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface RepaymentScheduleRepositoryPort {

    List<RepaymentSchedule> saveAll(List<RepaymentSchedule> schedules);

    List<RepaymentSchedule> findByAccountIdOrderByInstallmentNoAsc(UUID accountId);

    RepaymentSchedule save(RepaymentSchedule schedule);
}
