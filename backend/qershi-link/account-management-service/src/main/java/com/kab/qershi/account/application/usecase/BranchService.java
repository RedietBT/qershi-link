package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Branch;
import com.kab.qershi.account.domain.ports.inbound.BranchUseCase;
import com.kab.qershi.account.domain.ports.outbound.BranchRepositoryPort;
import com.kab.qershi.account.infrastructure.rest.dto.CreateBranchRequest;
import com.kab.qershi.account.infrastructure.rest.dto.UpdateBranchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service implementing BranchUseCase.
 * Manages SACCO branch lifecycle, metadata, discretionary limits, and vault GL assignments.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Service
@Transactional
public class BranchService implements BranchUseCase {

    private final BranchRepositoryPort branchRepository;

    public BranchService(BranchRepositoryPort branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Branch> getAllBranches(String status) {
        if (status != null && !status.isBlank()) {
            return branchRepository.findByStatus(status.toUpperCase().trim());
        }
        return branchRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getBranchById(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found for ID: " + branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public Branch getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found for code: " + branchCode));
    }

    @Override
    public Branch createBranch(CreateBranchRequest request) {
        String cleanCode = request.branchCode().trim();
        if (branchRepository.existsByBranchCode(cleanCode)) {
            throw new IllegalArgumentException("Branch with code '" + cleanCode + "' already exists.");
        }

        Branch branch = new Branch(
                UUID.randomUUID(),
                cleanCode,
                request.branchName().trim(),
                request.region() != null ? request.region().trim() : null,
                request.address() != null ? request.address().trim() : null,
                request.contactPhone() != null ? request.contactPhone().trim() : null,
                request.managerUserId(),
                request.vaultGlCode() != null && !request.vaultGlCode().isBlank() ? request.vaultGlCode().trim() : "1010-" + cleanCode,
                request.discretionaryLendingLimit() != null ? request.discretionaryLendingLimit() : new BigDecimal("100000.00"),
                "ACTIVE",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        return branchRepository.save(branch);
    }

    @Override
    public Branch updateBranch(UUID branchId, UpdateBranchRequest request) {
        Branch branch = getBranchById(branchId);

        branch.updateDetails(
                request.branchName(),
                request.region(),
                request.address(),
                request.contactPhone(),
                request.managerUserId(),
                request.vaultGlCode(),
                request.discretionaryLendingLimit(),
                request.status()
        );

        return branchRepository.save(branch);
    }

    @Override
    public Branch updateBranchStatus(UUID branchId, String status) {
        Branch branch = getBranchById(branchId);
        branch.updateStatus(status);
        return branchRepository.save(branch);
    }
}
