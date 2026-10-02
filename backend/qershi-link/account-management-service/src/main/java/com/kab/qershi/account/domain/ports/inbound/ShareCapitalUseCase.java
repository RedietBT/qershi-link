package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.ShareAccount;
import com.kab.qershi.account.domain.model.ShareCertificate;
import com.kab.qershi.account.domain.model.ShareTransfer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case Port for Share Capital Operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ShareCapitalUseCase {

    record ShareAccountSummary(
            UUID shareAccountId,
            UUID memberId,
            String accountNumber,
            int totalShares,
            BigDecimal nominalValue,
            BigDecimal totalAmount,
            String status,
            boolean isMandatoryRequirementMet,
            int minimumRequiredShares,
            List<ShareCertificate> certificates
    ) {}

    record PurchaseSharesResult(
            UUID shareAccountId,
            String accountNumber,
            String certificateNumber,
            long startSerial,
            long endSerial,
            int sharesPurchased,
            BigDecimal totalAmountPaid,
            int newTotalShares,
            BigDecimal newTotalAmount,
            String glJournalRef,
            boolean mandatoryRequirementMet
    ) {}

    record TransferSharesResult(
            UUID transferId,
            UUID fromMemberId,
            UUID toMemberId,
            int shareCount,
            BigDecimal transferPrice,
            String status,
            String certificateNumberIssued
    ) {}

    ShareAccount getOrCreateShareAccount(UUID memberId, String saccoCode, String branchCode);

    PurchaseSharesResult purchaseShares(UUID memberId, int shareCount, String sourceAccountNo, String saccoCode, String branchCode);

    TransferSharesResult transferShares(UUID fromMemberId, UUID toMemberId, UUID certificateId, int shareCount, BigDecimal transferPrice, boolean autoApprove, UUID approvedBy);

    TransferSharesResult approveTransfer(UUID transferId, UUID approverUserId);

    ShareAccountSummary getMemberShareAccountSummary(UUID memberId);

    List<ShareCertificate> getMemberCertificates(UUID memberId);

    List<ShareTransfer> getMemberTransfers(UUID memberId);

    List<ShareTransfer> getPendingTransfers();

    /** Lookup member share account summary by phone number */
    ShareAccountSummary getShareAccountByPhone(String phoneNumber);
}
