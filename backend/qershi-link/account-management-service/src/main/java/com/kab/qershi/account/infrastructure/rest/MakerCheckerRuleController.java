package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.infrastructure.persistence.MakerCheckerRuleEntity;
import com.kab.qershi.account.infrastructure.persistence.ProductMakerCheckerRuleEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataMakerCheckerRuleRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataProductMakerCheckerRuleRepository;
import com.kab.qershi.account.infrastructure.rest.dto.MakerCheckerRuleRequest;
import com.kab.qershi.account.infrastructure.rest.dto.ProductRuleCreateRequest;
import com.kab.qershi.account.infrastructure.rest.dto.ProductRuleUpdateRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing SACCO Maker-Checker Policies, Four-Eyes Thresholds,
 * Domain Role Clearances, and Per-Product Risk Limits.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/sacco-config/maker-checker-rules")
@Tag(name = "SACCO Maker-Checker Policy Rules", description = "Endpoints for configuring Four-Eyes workflow toggles, transaction limits, anti-self-approval enforcement, and product-specific limits.")
@SecurityRequirement(name = "bearerAuth")
public class MakerCheckerRuleController {

    private final SpringDataMakerCheckerRuleRepository ruleRepository;
    private final SpringDataProductMakerCheckerRuleRepository productRuleRepository;

    public MakerCheckerRuleController(SpringDataMakerCheckerRuleRepository ruleRepository,
                                      SpringDataProductMakerCheckerRuleRepository productRuleRepository) {
        this.ruleRepository = ruleRepository;
        this.productRuleRepository = productRuleRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAnyAuthority('SACCO_CONFIG', 'ACCOUNT_VIEW')")
    @Operation(summary = "Get Maker-Checker Rules", description = "Retrieves active Four-Eyes governance toggles, transaction supervisor limits, and domain role assignments.")
    public ResponseEntity<ApiResponse<MakerCheckerRuleEntity>> getRules() {
        MakerCheckerRuleEntity rules = ruleRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> {
                    MakerCheckerRuleEntity def = new MakerCheckerRuleEntity();
                    return ruleRepository.save(def);
                });
        return ResponseEntity.ok(ApiResponse.success(rules, "Maker-Checker policy rules retrieved successfully."));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('SACCO_CONFIG', 'ROLE_MANAGE')")
    @Operation(summary = "Update Maker-Checker Rules", description = "Configures Four-Eyes governance workflows, supervisor transaction thresholds, and domain Maker/Checker role clearances.")
    public ResponseEntity<ApiResponse<MakerCheckerRuleEntity>> updateRules(@Valid @RequestBody MakerCheckerRuleRequest request) {
        MakerCheckerRuleEntity rules = ruleRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(MakerCheckerRuleEntity::new);

        if (request.enableMemberOnboardingChecker() != null) {
            rules.setEnableMemberOnboardingChecker(request.enableMemberOnboardingChecker());
        }
        if (request.enableAccountOpeningChecker() != null) {
            rules.setEnableAccountOpeningChecker(request.enableAccountOpeningChecker());
        }
        if (request.enableAccountFreezeChecker() != null) {
            rules.setEnableAccountFreezeChecker(request.enableAccountFreezeChecker());
        }
        if (request.enableLoanApprovalChecker() != null) {
            rules.setEnableLoanApprovalChecker(request.enableLoanApprovalChecker());
        }
        if (request.enableLoanDisbursementChecker() != null) {
            rules.setEnableLoanDisbursementChecker(request.enableLoanDisbursementChecker());
        }
        if (request.transactionCheckerThreshold() != null) {
            rules.setTransactionCheckerThreshold(request.transactionCheckerThreshold());
        }
        if (request.dailyAccountLimitThreshold() != null) {
            rules.setDailyAccountLimitThreshold(request.dailyAccountLimitThreshold());
        }
        if (request.enforceAntiSelfApproval() != null) {
            rules.setEnforceAntiSelfApproval(request.enforceAntiSelfApproval());
        }
        if (request.memberMakerRoles() != null) {
            rules.setMemberMakerRoles(request.memberMakerRoles().trim());
        }
        if (request.memberCheckerRoles() != null) {
            rules.setMemberCheckerRoles(request.memberCheckerRoles().trim());
        }
        if (request.accountMakerRoles() != null) {
            rules.setAccountMakerRoles(request.accountMakerRoles().trim());
        }
        if (request.accountCheckerRoles() != null) {
            rules.setAccountCheckerRoles(request.accountCheckerRoles().trim());
        }
        if (request.loanMakerRoles() != null) {
            rules.setLoanMakerRoles(request.loanMakerRoles().trim());
        }
        if (request.loanCheckerRoles() != null) {
            rules.setLoanCheckerRoles(request.loanCheckerRoles().trim());
        }

        MakerCheckerRuleEntity saved = ruleRepository.save(rules);
        return ResponseEntity.ok(ApiResponse.success(saved, "SACCO Maker-Checker policy rules updated successfully."));
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR') or hasAnyAuthority('SACCO_CONFIG', 'ACCOUNT_VIEW')")
    @Operation(summary = "Get Per-Product Risk & Maker-Checker Rules", description = "Retrieves all account product limits, storing balance caps, and Four-Eyes settings.")
    public ResponseEntity<ApiResponse<List<ProductMakerCheckerRuleEntity>>> getProductRules() {
        List<ProductMakerCheckerRuleEntity> productRules = productRuleRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(productRules, "Retrieved " + productRules.size() + " product rules."));
    }

    @PutMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('SACCO_CONFIG', 'PRODUCT_MANAGE')")
    @Operation(summary = "Update Specific Product Rules", description = "Updates max balance storing limit, single withdrawal limit, daily limit, and Four-Eyes override for an account product.")
    public ResponseEntity<ApiResponse<ProductMakerCheckerRuleEntity>> updateProductRule(
            @PathVariable String productCode,
            @Valid @RequestBody ProductRuleUpdateRequest request) {
        ProductMakerCheckerRuleEntity rule = productRuleRepository.findByProductCode(productCode)
                .orElseGet(() -> {
                    ProductMakerCheckerRuleEntity newRule = new ProductMakerCheckerRuleEntity();
                    newRule.setProductCode(productCode);
                    newRule.setProductName("Product " + productCode);
                    return newRule;
                });

        rule.setMinOperatingBalance(request.minOperatingBalance());
        rule.setMaxBalanceLimit(request.maxBalanceLimit());
        rule.setSingleWithdrawalLimit(request.singleWithdrawalLimit());
        rule.setDailyWithdrawalLimit(request.dailyWithdrawalLimit());
        if (request.enableMakerChecker() != null) {
            rule.setEnableMakerChecker(request.enableMakerChecker());
        }

        ProductMakerCheckerRuleEntity saved = productRuleRepository.save(rule);
        return ResponseEntity.ok(ApiResponse.success(saved, "Product rules for " + productCode + " updated successfully."));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('SACCO_CONFIG', 'PRODUCT_MANAGE')")
    @Operation(summary = "Create Product Risk & Transaction Rule", description = "Defines transaction limits, max balance storing limit, and Maker-Checker for an account product type.")
    public ResponseEntity<ApiResponse<ProductMakerCheckerRuleEntity>> createProductRule(
            @Valid @RequestBody ProductRuleCreateRequest request) {
        if (productRuleRepository.findByProductCode(request.productCode().trim()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Rule for product code " + request.productCode() + " already exists."));
        }

        ProductMakerCheckerRuleEntity rule = new ProductMakerCheckerRuleEntity();
        rule.setProductCode(request.productCode().trim());
        rule.setProductName(request.productName().trim());
        rule.setCategory(request.category() != null ? request.category().trim() : "SAVINGS");
        rule.setMinOperatingBalance(request.minOperatingBalance());
        rule.setMaxBalanceLimit(request.maxBalanceLimit());
        rule.setSingleWithdrawalLimit(request.singleWithdrawalLimit());
        rule.setDailyWithdrawalLimit(request.dailyWithdrawalLimit());
        rule.setEnableMakerChecker(request.enableMakerChecker() != null ? request.enableMakerChecker() : true);

        ProductMakerCheckerRuleEntity saved = productRuleRepository.save(rule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Rule for product " + request.productName() + " created successfully."));
    }

    @DeleteMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('SACCO_CONFIG', 'PRODUCT_MANAGE')")
    @Operation(summary = "Delete Product Risk Rule", description = "Removes a specific product risk rule configuration.")
    public ResponseEntity<ApiResponse<Void>> deleteProductRule(@PathVariable String productCode) {
        var existing = productRuleRepository.findByProductCode(productCode);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Product rule not found: " + productCode));
        }
        productRuleRepository.delete(existing.get());
        return ResponseEntity.ok(ApiResponse.success(null, "Product rule " + productCode + " deleted successfully."));
    }
}

