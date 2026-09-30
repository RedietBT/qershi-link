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
 * Implements strict cyclic graph protection and depth limits to prevent Denial of Service (DoS).
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Service
public class ChartOfAccountService {

    private static final Logger log = LoggerFactory.getLogger(ChartOfAccountService.class);
    private static final int MAX_HIERARCHY_DEPTH = 6;

    private final SpringDataChartOfAccountRepository coaRepository;

    public ChartOfAccountService(SpringDataChartOfAccountRepository coaRepository) {
        this.coaRepository = coaRepository;
    }

    /**
     * Builds and returns the complete Chart of Accounts hierarchy as a recursive tree.
     * Computes the aggregated rollup balance for every category and branch node.
     * Guards against cyclic references using a visited set.
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

        // 3. Calculate recursive rollup balances with cycle detection
        Set<String> visited = new HashSet<>();
        for (ChartOfAccountNodeDto root : rootNodes) {
            computeRollupBalance(root, visited);
        }

        return rootNodes;
    }

    private BigDecimal computeRollupBalance(ChartOfAccountNodeDto node, Set<String> visited) {
        if (node == null || visited.contains(node.getGlCode())) {
            log.warn("Cycle or duplicate traversal detected at GL Code '{}'. Aborting branch traversal.",
                    node != null ? node.getGlCode() : "null");
            return BigDecimal.ZERO;
        }

        visited.add(node.getGlCode());
        BigDecimal sum = node.getBalance() != null ? node.getBalance() : BigDecimal.ZERO;

        for (ChartOfAccountNodeDto child : node.getChildren()) {
            sum = sum.add(computeRollupBalance(child, visited));
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
     * Enforces:
     * - GL Code uniqueness
     * - Parent existence and self-reference blocking
     * - Circular loop prevention
     * - Maximum hierarchy depth of 6
     * - Structural category consistency (child type must match parent type)
     * - Zero balance initialization (tamper protection)
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
            if (cleanGlCode.equalsIgnoreCase(parentGl)) {
                throw new IllegalArgumentException("Security/Integrity Error: An account cannot be its own parent.");
            }

            ChartOfAccountEntity parent = coaRepository.findByGlCode(parentGl)
                    .orElseThrow(() -> new IllegalArgumentException("Parent GL Code '" + parentGl + "' not found."));

            // Enforce structural accounting integrity: Child must match parent category type
            if (parent.getAccountType() != req.getAccountType()) {
                throw new IllegalArgumentException(
                        "Accounting Integrity Violation: Sub-account type (" + req.getAccountType() +
                        ") must match parent account type (" + parent.getAccountType() + ")."
                );
            }

            // Enforce hierarchy depth limit to prevent infinite tree bloat
            int depth = 1;
            String ancestorGl = parent.getParentGlCode();
            Set<String> ancestors = new HashSet<>();
            ancestors.add(parent.getGlCode());

            while (ancestorGl != null && !ancestorGl.trim().isEmpty()) {
                depth++;
                if (depth > MAX_HIERARCHY_DEPTH) {
                    throw new IllegalArgumentException("Maximum Chart of Accounts hierarchy depth of " +
                            MAX_HIERARCHY_DEPTH + " exceeded.");
                }
                if (ancestors.contains(ancestorGl)) {
                    throw new IllegalStateException("Corrupt hierarchy detected: Cycle present among ancestors of " + parentGl);
                }
                ancestors.add(ancestorGl);

                Optional<ChartOfAccountEntity> ancestorEntity = coaRepository.findByGlCode(ancestorGl);
                ancestorGl = ancestorEntity.map(ChartOfAccountEntity::getParentGlCode).orElse(null);
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

        // Security Guard: New GL accounts must ALWAYS start with zero balance
        entity.setBalance(BigDecimal.ZERO);

        ChartOfAccountEntity saved = coaRepository.save(entity);
        log.info("Successfully registered new GL account '{}' ({}) under parent '{}'",
                saved.getGlCode(), saved.getAccountName(), parentGl);

        return saved;
    }
}
