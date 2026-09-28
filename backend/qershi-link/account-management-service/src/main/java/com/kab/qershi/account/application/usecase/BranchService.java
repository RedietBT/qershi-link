package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.BranchEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataBranchRepository;
import com.kab.qershi.account.infrastructure.rest.dto.CreateBranchRequest;
import com.kab.qershi.account.infrastructure.rest.dto.UpdateBranchRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service managing SACCO branch lifecycle, metadata, discretionary limits, and vault GL assignments.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class BranchService {

    private final SpringDataBranchRepository branchRepository;

    public BranchService(SpringDataBranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchEntity> getAllBranches(String status) {
        if (status != null && !status.isBlank()) {
            return branchRepository.findByStatus(status.toUpperCase().trim());
        }
        return branchRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BranchEntity getBranchById(UUID branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found for ID: " + branchId));
    }

    @Transactional(readOnly = true)
    public BranchEntity getBranchByCode(String branchCode) {
        return branchRepository.findByBranchCode(branchCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found for code: " + branchCode));
    }

    public BranchEntity createBranch(CreateBranchRequest request) {
        String cleanCode = request.branchCode().trim();
        if (branchRepository.existsByBranchCode(cleanCode)) {
            throw new IllegalArgumentException("Branch with code '" + cleanCode + "' already exists.");
        }

        BranchEntity entity = new BranchEntity(
                null,
                cleanCode,
                request.branchName().trim(),
                request.region() != null ? request.region().trim() : null,
                request.address() != null ? request.address().trim() : null,
                request.contactPhone() != null ? request.contactPhone().trim() : null,
                request.managerUserId(),
                request.vaultGlCode() != null && !request.vaultGlCode().isBlank() ? request.vaultGlCode().trim() : "1010-" + cleanCode,
                request.discretionaryLendingLimit() != null ? request.discretionaryLendingLimit() : new BigDecimal("100000.00"),
                "ACTIVE"
        );

        return branchRepository.save(entity);
    }

    public BranchEntity updateBranch(UUID branchId, UpdateBranchRequest request) {
        BranchEntity entity = getBranchById(branchId);

        entity.setBranchName(request.branchName().trim());
        if (request.region() != null) entity.setRegion(request.region().trim());
        if (request.address() != null) entity.setAddress(request.address().trim());
        if (request.contactPhone() != null) entity.setContactPhone(request.contactPhone().trim());
        if (request.managerUserId() != null) entity.setManagerUserId(request.managerUserId());
        if (request.vaultGlCode() != null && !request.vaultGlCode().isBlank()) entity.setVaultGlCode(request.vaultGlCode().trim());
        if (request.discretionaryLendingLimit() != null) entity.setDiscretionaryLendingLimit(request.discretionaryLendingLimit());
        if (request.status() != null && !request.status().isBlank()) {
            entity.setStatus(request.status().toUpperCase().trim());
        }

        return branchRepository.save(entity);
    }

    public BranchEntity updateBranchStatus(UUID branchId, String status) {
        BranchEntity entity = getBranchById(branchId);
        String cleanStatus = status.toUpperCase().trim();
        if (!"ACTIVE".equals(cleanStatus) && !"INACTIVE".equals(cleanStatus)) {
            throw new IllegalArgumentException("Status must be either ACTIVE or INACTIVE");
        }
        entity.setStatus(cleanStatus);
        return branchRepository.save(entity);
    }
}
