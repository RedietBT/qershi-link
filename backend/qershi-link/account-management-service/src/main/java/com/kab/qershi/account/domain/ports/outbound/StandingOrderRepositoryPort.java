package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.StandingOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Repository Port for Standing Order persistence.
 * Depends only on Domain Models — no JPA coupling.
 *
 * @author KAB Digital Solution PLC
 */
public interface StandingOrderRepositoryPort {

    StandingOrder save(StandingOrder standingOrder);
    Optional<StandingOrder> findById(UUID id);
    Optional<StandingOrder> findByStandingOrderNo(String standingOrderNo);
    List<StandingOrder> findByMemberId(UUID memberId);
    List<StandingOrder> findBySourceAccountId(UUID sourceAccountId);

    /** Core sweep query — all ACTIVE orders whose nextRunDate <= today */
    List<StandingOrder> findDueOrders(LocalDate asOfDate);

    List<StandingOrder> findAll();
}
