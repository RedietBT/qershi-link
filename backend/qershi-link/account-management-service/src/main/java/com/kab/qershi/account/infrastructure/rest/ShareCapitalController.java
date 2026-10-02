package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.ShareCertificate;
import com.kab.qershi.account.domain.model.ShareTransfer;
import com.kab.qershi.account.domain.ports.inbound.ShareCapitalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller — Share Capital Management.
 * Every endpoint guards by BOTH role AND authority (permission) so that
 * fine-grained permission assignments work independently of role membership.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/share-capital")
@Tag(name = "Share Capital", description = "SACCO mandatory share capital — purchase, certificate tracking, peer transfers with four-eyes approval")
public class ShareCapitalController {

    private final ShareCapitalUseCase shareCapitalUseCase;

    public ShareCapitalController(ShareCapitalUseCase shareCapitalUseCase) {
        this.shareCapitalUseCase = shareCapitalUseCase;
    }

    // ── 1. Member Share Account Summary ───────────────────────────────────────

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER','AUDITOR') or hasAnyAuthority('SHARE_CAPITAL_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "Get member share account summary")
    public ResponseEntity<ShareCapitalUseCase.ShareAccountSummary> getMemberShareSummary(
            @PathVariable UUID memberId) {
        return ResponseEntity.ok(shareCapitalUseCase.getMemberShareAccountSummary(memberId));
    }

    // ── 2. Get / Open Share Account ───────────────────────────────────────────

    @PostMapping("/member/{memberId}/account")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('SHARE_CAPITAL_MANAGE','ACCOUNT_CREATE')")
    @Operation(summary = "Open or retrieve share account — idempotent")
    public ResponseEntity<Map<String, Object>> getOrCreateShareAccount(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "DEFAULT") String saccoCode,
            @RequestParam(required = false) String branchCode) {
        var account = shareCapitalUseCase.getOrCreateShareAccount(memberId, saccoCode, branchCode);
        return ResponseEntity.ok(Map.of(
                "shareAccountId", account.getId(),
                "accountNumber", account.getAccountNumber(),
                "totalShares", account.getTotalShares(),
                "totalAmount", account.getTotalAmount(),
                "status", account.getStatus(),
                "mandatoryRequirementMet", account.isMandatoryRequirementMet()
        ));
    }

    // ── 3. Purchase Shares ────────────────────────────────────────────────────

    @PostMapping("/member/{memberId}/purchase")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('SHARE_CAPITAL_MANAGE','ACCOUNT_CREATE')")
    @Operation(summary = "Purchase shares — debits member savings and issues share certificate")
    public ResponseEntity<ShareCapitalUseCase.PurchaseSharesResult> purchaseShares(
            @PathVariable UUID memberId,
            @RequestBody Map<String, Object> body) {
        int shareCount = ((Number) body.getOrDefault("shareCount", 1)).intValue();
        String sourceAccountNo = (String) body.get("sourceAccountNo");
        String saccoCode  = (String) body.getOrDefault("saccoCode", "DEFAULT");
        String branchCode = (String) body.get("branchCode");
        var result = shareCapitalUseCase.purchaseShares(memberId, shareCount, sourceAccountNo, saccoCode, branchCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ── 4. Lookup by Phone Number ─────────────────────────────────────────────

    @GetMapping("/lookup/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER') or hasAnyAuthority('SHARE_CAPITAL_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "Lookup share account by member phone number",
               description = "Resolves a member's share account when the account number is unknown. Returns share summary.")
    public ResponseEntity<?> lookupByPhone(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(shareCapitalUseCase.getShareAccountByPhone(phoneNumber));
    }

    // ── 5. Get Certificates ───────────────────────────────────────────────────

    @GetMapping("/member/{memberId}/certificates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','TELLER','AUDITOR') or hasAnyAuthority('SHARE_CAPITAL_VIEW','ACCOUNT_VIEW')")
    @Operation(summary = "List share certificates with serial number ranges")
    public ResponseEntity<List<ShareCertificate>> getMemberCertificates(@PathVariable UUID memberId) {
        return ResponseEntity.ok(shareCapitalUseCase.getMemberCertificates(memberId));
    }

    // ── 6. Request Share Transfer ─────────────────────────────────────────────

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN') or hasAnyAuthority('SHARE_CAPITAL_MANAGE','SHARE_TRANSFER_APPROVE')")
    @Operation(summary = "Initiate peer-to-peer share transfer — creates pending or auto-approves")
    public ResponseEntity<ShareCapitalUseCase.TransferSharesResult> transferShares(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        UUID fromMemberId  = UUID.fromString((String) body.get("fromMemberId"));
        UUID toMemberId    = UUID.fromString((String) body.get("toMemberId"));
        UUID certificateId = UUID.fromString((String) body.get("certificateId"));
        int shareCount     = ((Number) body.getOrDefault("shareCount", 1)).intValue();
        BigDecimal price   = body.containsKey("transferPrice")
                ? new BigDecimal(body.get("transferPrice").toString()) : null;
        boolean autoApprove = Boolean.TRUE.equals(body.get("autoApprove"));
        var result = shareCapitalUseCase.transferShares(
                fromMemberId, toMemberId, certificateId, shareCount, price, autoApprove, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ── 7. Approve Transfer ───────────────────────────────────────────────────

    @PatchMapping("/transfer/{transferId}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN') or hasAnyAuthority('SHARE_TRANSFER_APPROVE')")
    @Operation(summary = "Four-eyes approval for a pending share transfer")
    public ResponseEntity<ShareCapitalUseCase.TransferSharesResult> approveTransfer(
            @PathVariable UUID transferId,
            Authentication authentication) {
        UUID approverUserId = extractUserId(authentication);
        return ResponseEntity.ok(shareCapitalUseCase.approveTransfer(transferId, approverUserId));
    }

    // ── 8. Pending Transfers ──────────────────────────────────────────────────

    @GetMapping("/transfer/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN') or hasAnyAuthority('SHARE_TRANSFER_APPROVE','SHARE_CAPITAL_VIEW')")
    @Operation(summary = "List pending share transfers awaiting approval")
    public ResponseEntity<List<ShareTransfer>> getPendingTransfers() {
        return ResponseEntity.ok(shareCapitalUseCase.getPendingTransfers());
    }

    // ── 9. Member Transfer History ────────────────────────────────────────────

    @GetMapping("/member/{memberId}/transfers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','SACCO_ADMIN','AUDITOR') or hasAnyAuthority('SHARE_CAPITAL_VIEW','AUDIT_LOG_VIEW')")
    @Operation(summary = "Get all transfers for a member (incoming + outgoing)")
    public ResponseEntity<List<ShareTransfer>> getMemberTransfers(@PathVariable UUID memberId) {
        return ResponseEntity.ok(shareCapitalUseCase.getMemberTransfers(memberId));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        try {
            if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                try { return UUID.fromString(ud.getUsername()); } catch (Exception ex) { return null; }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
