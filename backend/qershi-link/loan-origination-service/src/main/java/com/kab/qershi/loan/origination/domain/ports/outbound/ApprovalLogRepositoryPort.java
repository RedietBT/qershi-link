package com.kab.qershi.loan.origination.domain.ports.outbound;

import com.kab.qershi.loan.origination.domain.model.ApprovalLog;

import java.util.List;
import java.util.UUID;

/**
 * Outbound repository port for Maker-Checker approval logs persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ApprovalLogRepositoryPort {

    ApprovalLog save(ApprovalLog log);

    List<ApprovalLog> findByApplicationId(UUID applicationId);
}
