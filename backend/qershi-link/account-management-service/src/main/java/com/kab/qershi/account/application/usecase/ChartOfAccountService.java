package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.ChartOfAccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataChartOfAccountRepository;
import com.kab.qershi.account.infrastructure.rest.dto.ChartOfAccountNodeDto;
import com.kab.qershi.account.infrastructure.rest.dto.CreateChartOfAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Application service for managing the General Ledger Chart of Accounts (COA).
 * Builds dynamic recursive hierarchy trees, calculates rollup balances, and manages GL account creation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class ChartOfAccountService {

    private static final Logger log = LoggerFactory.getLogger(ChartOfAccountService.class);

    private final SpringDataChartOfAccountRepository coaRepository;

    public ChartOfAccountService(SpringDataChartOfAccountRepository coaRepository) {
        this.coaRepository = coaRepository;
    }

    /**
     * Builds and returns the complete Chart of Accounts hierarchy as a recursive tree.
     * Computes the aggregated rollup balance for every category and branch node.
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountNodeDto> getCoaTree() {
        List<ChartOfAccountEntity> allAccounts = coaRepository.findAllByOrderByGlCodeAsc();
        if (allAccounts.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Map entities to DTO nodes
        Map<String, ChartOfAccountNodeDto> nodeMap = new LinkedHashMap<>();
        for (ChartOfAccountEntity entity : allAccounts) {
            ChartOfAccountNodeDto node = new ChartOfAccountNodeDto(
                    entity.getAccountId(),
                    entity.getGlCode(),
                    entity.getAccountName(),
                    entity.getAccountType(),
                    entity.getParentGlCode(),
                    entity.getBalance(),
                    entity.getStatus(),
                    entity.getIsReconciled(),
                    entity.getAllowManualJournal(),
                    entity.getDescription()
            );
            nodeMap.put(entity.getGlCode(), node);
        }

        // 2. Build parent-child relationships
        List<ChartOfAccountNodeDto> rootNodes = new ArrayList<>();
        for (ChartOfAccountNodeDto node : nodeMap.values()) {
            String parentGl = node.getParentGlCode();
            if (parentGl == null || parentGl.trim().isEmpty() || !nodeMap.containsKey(parentGl)) {
                rootNodes.add(node);
            } else {
                ChartOfAccountNodeDto parent = nodeMap.get(parentGl);
                parent.getChildren().add(node);
            }
        }

        // 3. Calculate recursive rollup balances for each root branch
        for (ChartOfAccountNodeDto root : rootNodes) {
            computeRollupBalance(root);
        }

        return rootNodes;
    }

    private BigDecimal computeRollupBalance(ChartOfAccountNodeDto node) {
        BigDecimal sum = node.getBalance() != null ? node.getBalance() : BigDecimal.ZERO;
        for (ChartOfAccountNodeDto child : node.getChildren()) {
            sum = sum.add(computeRollupBalance(child));
        }
        node.setRollupBalance(sum);
        return sum;
    }

    /**
     * Retrieves all General Ledger accounts in flat order.
     */
    @Transactional(readOnly = true)
    public List<ChartOfAccountEntity> getAllFlat() {
        return coaRepository.findAllByOrderByGlCodeAsc();
    }

    /**
     * Creates a new General Ledger account.
     * Validates GL Code uniqueness, parent validity, and structural type consistency.
     */
    @Transactional
    public ChartOfAccountEntity createAccount(CreateChartOfAccountRequest req) {
        String cleanGlCode = req.getGlCode().trim();
        if (coaRepository.existsByGlCode(cleanGlCode)) {
            throw new IllegalArgumentException("GL Code '" + cleanGlCode + "' already exists in Chart of Accounts.");
        }

        String parentGl = req.getParentGlCode() != null && !req.getParentGlCode().trim().isEmpty()
                ? req.getParentGlCode().trim()
                : null;

        if (parentGl != null) {
            ChartOfAccountEntity parent = coaRepository.findByGlCode(parentGl)
                    .orElseThrow(() -> new IllegalArgumentException("Parent GL Code '" + parentGl + "' not found."));

            // Enforce structural accounting integrity: Child must match parent category type
            if (parent.getAccountType() != req.getAccountType()) {
                throw new IllegalArgumentException(
                        "Accounting Integrity Violation: Sub-account type (" + req.getAccountType() +
                        ") must match parent account type (" + parent.getAccountType() + ")."
                );
            }
        }

        ChartOfAccountEntity entity = new ChartOfAccountEntity(
                cleanGlCode,
                req.getAccountName().trim(),
                req.getAccountType(),
                parentGl,
                req.getDescription() != null ? req.getDescription().trim() : null
        );

        if (req.getAllowManualJournal() != null) {
            entity.setAllowManualJournal(req.getAllowManualJournal());
        }

        ChartOfAccountEntity saved = coaRepository.save(entity);
        log.info("Created new General Ledger account '{}' ({}) under parent '{}'",
                saved.getGlCode(), saved.getAccountName(), parentGl);

        return saved;
    }
}
