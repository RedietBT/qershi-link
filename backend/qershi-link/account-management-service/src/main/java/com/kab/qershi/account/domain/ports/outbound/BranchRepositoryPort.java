package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.Branch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Branch persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface BranchRepositoryPort {

    Branch save(Branch branch);

    Optional<Branch> findById(UUID branchId);

    Optional<Branch> findByBranchCode(String branchCode);

    List<Branch> findAll();

    List<Branch> findByStatus(String status);

    boolean existsByBranchCode(String branchCode);
}
