package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.infrastructure.persistence.MakerCheckerRuleEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataMakerCheckerRuleRepository;
import com.kab.qershi.account.infrastructure.rest.dto.MakerCheckerRuleRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing SACCO Maker-Checker Policies & Four-Eyes Thresholds.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/sacco-config/maker-checker-rules")
@Tag(name = "SACCO Maker-Checker Policy Rules", description = "Endpoints for configuring Four-Eyes workflow toggles, transaction limits, and anti-self-approval enforcement.")
@SecurityRequirement(name = "bearerAuth")
public class MakerCheckerRuleController {

    private final SpringDataMakerCheckerRuleRepository ruleRepository;

    public MakerCheckerRuleController(SpringDataMakerCheckerRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAnyAuthority('SACCO_CONFIG', 'ACCOUNT_VIEW')")
    @Operation(summary = "Get Maker-Checker Rules", description = "Retrieves active Four-Eyes governance toggles, transaction supervisor limits, and anti-self-approval status.")
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
    @Operation(summary = "Update Maker-Checker Rules", description = "Configures Four-Eyes governance workflows, supervisor transaction thresholds, and anti-self-approval enforcement.")
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

        MakerCheckerRuleEntity saved = ruleRepository.save(rules);
        return ResponseEntity.ok(ApiResponse.success(saved, "SACCO Maker-Checker policy rules updated successfully."));
    }
}
