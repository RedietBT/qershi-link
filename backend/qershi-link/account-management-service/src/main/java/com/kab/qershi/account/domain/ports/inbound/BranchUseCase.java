package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.Branch;
import com.kab.qershi.account.infrastructure.rest.dto.CreateBranchRequest;
import com.kab.qershi.account.infrastructure.rest.dto.UpdateBranchRequest;

import java.util.List;
import java.util.UUID;

/**
 * Inbound use case port for managing SACCO branch offices.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface BranchUseCase {

    List<Branch> getAllBranches(String status);

    Branch getBranchById(UUID branchId);

    Branch getBranchByCode(String branchCode);

    Branch createBranch(CreateBranchRequest request);

    Branch updateBranch(UUID branchId, UpdateBranchRequest request);

    Branch updateBranchStatus(UUID branchId, String status);
}
