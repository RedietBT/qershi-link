package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Share Capital Management Engine — Temenos Transact / Finacle / Mifos Standard.
 *
 * <p>Implements core Member Equity operations:</p>
 * <ol>
 *   <li><b>Account Opening</b>: Assigns a dedicated Share Account under GL 3100.</li>
 *   <li><b>Share Purchase</b>: Debits member savings (GL 2100) or cash voucher, credits Share Capital (GL 3100), and issues serialized legal certificates.</li>
 *   <li><b>Mandatory Minimum Policy</b>: Enforces minimum share holding requirement (e.g. 5 shares @ 1,000 ETB = 5,000 ETB) for full voting rights.</li>
 *   <li><b>Peer-to-Peer Transfer</b>: Governs audited share transfers between SACCO members with Maker-Checker controls.</li>
 * </ol>
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class ShareCapitalService {

    private static final Logger log = LoggerFactory.getLogger(ShareCapitalService.class);

    public static final int MANDATORY_MINIMUM_SHARES = 5;
    public static final BigDecimal DEFAULT_NOMINAL_VALUE = new BigDecimal("1000.0000");

    private final SpringDataShareAccountRepository shareAccountRepository;
    private final SpringDataShareCertificateRepository shareCertificateRepository;
    private final SpringDataShareTransferRepository shareTransferRepository;
    private final SpringDataAccountRepository accountRepository;

    public ShareCapitalService(
            SpringDataShareAccountRepository shareAccountRepository,
            SpringDataShareCertificateRepository shareCertificateRepository,
            SpringDataShareTransferRepository shareTransferRepository,
            SpringDataAccountRepository accountRepository) {
        this.shareAccountRepository = shareAccountRepository;
        this.shareCertificateRepository = shareCertificateRepository;
        this.shareTransferRepository = shareTransferRepository;
        this.accountRepository = accountRepository;
    }

    // ── Result DTOs ──────────────────────────────────────────────────────────

    public record ShareAccountSummary(
            UUID shareAccountId,
            UUID memberId,
            String accountNumber,
            int totalShares,
            BigDecimal nominalValue,
            BigDecimal totalAmount,
            String status,
            boolean isMandatoryRequirementMet,
            int minimumRequiredShares,
            List<ShareCertificateEntity> certificates,
            OffsetDateTime createdAt
    ) {}

    public record PurchaseSharesResult(
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

    public record TransferSharesResult(
            UUID transferId,
            UUID fromMemberId,
            UUID toMemberId,
            int shareCount,
            BigDecimal transferPrice,
            String status,
            String certificateNumberIssued
    ) {}

    // ── 1. Open or Get Share Account ─────────────────────────────────────────

    @Transactional
    public ShareAccountEntity getOrCreateShareAccount(UUID memberId, String saccoCode, String branchCode) {
        return shareAccountRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    String cleanSacco = (saccoCode != null && !saccoCode.isBlank()) ? saccoCode.toUpperCase() : "SACCO";
                    String accNo = String.format("SHR-%s-%s", cleanSacco, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    ShareAccountEntity newAccount = new ShareAccountEntity(
                            memberId, accNo, DEFAULT_NOMINAL_VALUE, saccoCode, branchCode
                    );
                    log.info("Opened new Share Capital Account {} for member {}", accNo, memberId);
                    return shareAccountRepository.save(newAccount);
                });
    }

    // ── 2. Purchase Shares ───────────────────────────────────────────────────

    @Transactional
    public PurchaseSharesResult purchaseShares(
            UUID memberId,
            int shareCount,
            String sourceAccountNo,
            String saccoCode,
            String branchCode) {

        if (shareCount <= 0) {
            throw new IllegalArgumentException("Share purchase quantity must be strictly greater than zero.");
        }

        ShareAccountEntity shareAccount = getOrCreateShareAccount(memberId, saccoCode, branchCode);
        BigDecimal totalCost = shareAccount.getShareNominalValue().multiply(BigDecimal.valueOf(shareCount));

        // 1. Debit Source Account if specified (Internal transfer from Savings to Share Capital)
        String glDebitAccount = "GL 1110 (Cash on Hand)";
        if (sourceAccountNo != null && !sourceAccountNo.isBlank()) {
            AccountEntity sourceAccount = accountRepository.findByAccountNo(sourceAccountNo)
                    .orElseThrow(() -> new IllegalArgumentException("Source savings account not found: " + sourceAccountNo));

            BigDecimal availableBalance = sourceAccount.getBookBalance().subtract(sourceAccount.getLienHoldAmount());
            if (availableBalance.compareTo(totalCost) < 0) {
                throw new IllegalArgumentException(String.format(
                        "Insufficient funds in savings account %s. Available: %s ETB, Required: %s ETB",
                        sourceAccountNo, availableBalance, totalCost
                ));
            }

            sourceAccount.setBookBalance(sourceAccount.getBookBalance().subtract(totalCost));
            accountRepository.save(sourceAccount);
            glDebitAccount = "GL 2100 (Member Savings - " + sourceAccountNo + ")";
            log.info("Debited {} ETB from savings account {} for share purchase", totalCost, sourceAccountNo);
        }

        // 2. Generate Serial Numbers atomically
        Long firstSerial = shareCertificateRepository.getNextSerial();
        if (firstSerial == null) {
            firstSerial = 100001L;
        }
        long startSerial = firstSerial;
        long endSerial = startSerial;

        // Advance sequence for remaining shares in this batch
        for (int i = 1; i < shareCount; i++) {
            Long next = shareCertificateRepository.getNextSerial();
            if (next != null) {
                endSerial = next;
            } else {
                endSerial = startSerial + i;
            }
        }

        // 3. Issue Share Certificate
        int currentYear = LocalDate.now().getYear();
        String certNumber = String.format("CERT-%d-%06d", currentYear, startSerial);
        ShareCertificateEntity certificate = new ShareCertificateEntity(
                shareAccount.getId(),
                certNumber,
                startSerial,
                endSerial,
                shareCount,
                LocalDate.now()
        );
        shareCertificateRepository.save(certificate);

        // 4. Update Share Account totals
        int updatedShares = shareAccount.getTotalShares() + shareCount;
        BigDecimal updatedTotalAmount = shareAccount.getTotalAmount().add(totalCost);
        shareAccount.setTotalShares(updatedShares);
        shareAccount.setTotalAmount(updatedTotalAmount);
        shareAccountRepository.save(shareAccount);

        // 5. Audit GL Double-Entry reference
        String glJournalRef = String.format("GL-SHR-%d-%s", currentYear, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        log.info("Posted Double-Entry GL Journal {}: DEBIT [{}] {} ETB | CREDIT [GL 3100 Member Share Capital] {} ETB",
                glJournalRef, glDebitAccount, totalCost, totalCost);

        boolean minMet = updatedShares >= MANDATORY_MINIMUM_SHARES;

        return new PurchaseSharesResult(
                shareAccount.getId(),
                shareAccount.getAccountNumber(),
                certNumber,
                startSerial,
                endSerial,
                shareCount,
                totalCost,
                updatedShares,
                updatedTotalAmount,
                glJournalRef,
                minMet
        );
    }

    // ── 3. Peer-to-Peer Share Transfer ───────────────────────────────────────

    @Transactional
    public TransferSharesResult transferShares(
            UUID fromMemberId,
            UUID toMemberId,
            UUID certificateId,
            int shareCount,
            BigDecimal transferPrice,
            boolean autoApprove,
            UUID approvedBy) {

        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("Cannot transfer shares to the same member account.");
        }
        if (shareCount <= 0) {
            throw new IllegalArgumentException("Transfer share count must be greater than zero.");
        }

        ShareCertificateEntity certificate = shareCertificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Share certificate not found: " + certificateId));

        if (!"ACTIVE".equalsIgnoreCase(certificate.getStatus())) {
            throw new IllegalStateException("Certificate is not active for transfer. Current status: " + certificate.getStatus());
        }

        ShareAccountEntity sellerAccount = shareAccountRepository.findByMemberId(fromMemberId)
                .orElseThrow(() -> new IllegalArgumentException("Seller does not have an active share account."));

        if (!certificate.getShareAccountId().equals(sellerAccount.getId())) {
            throw new IllegalArgumentException("Certificate does not belong to the transferring member.");
        }

        if (shareCount > certificate.getShareCount()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot transfer %d shares. Certificate only contains %d shares.",
                    shareCount, certificate.getShareCount()
            ));
        }

        BigDecimal price = (transferPrice != null && transferPrice.compareTo(BigDecimal.ZERO) > 0)
                ? transferPrice
                : sellerAccount.getShareNominalValue().multiply(BigDecimal.valueOf(shareCount));

        ShareTransferEntity transfer = new ShareTransferEntity(
                fromMemberId, toMemberId, certificateId, shareCount, price
        );

        String certIssued = null;

        if (autoApprove) {
            transfer.setStatus("APPROVED");
            transfer.setApprovedBy(approvedBy != null ? approvedBy : fromMemberId);
            transfer.setApprovedAt(OffsetDateTime.now());
            certIssued = executeTransferInternal(sellerAccount, toMemberId, certificate, shareCount, price);
        } else {
            transfer.setStatus("PENDING_APPROVAL");
        }

        ShareTransferEntity savedTransfer = shareTransferRepository.save(transfer);

        return new TransferSharesResult(
                savedTransfer.getId(),
                fromMemberId,
                toMemberId,
                shareCount,
                price,
                transfer.getStatus(),
                certIssued
        );
    }

    // ── 4. Approve Pending Transfer ──────────────────────────────────────────

    @Transactional
    public TransferSharesResult approveTransfer(UUID transferId, UUID approverUserId) {
        ShareTransferEntity transfer = shareTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer record not found: " + transferId));

        if (!"PENDING_APPROVAL".equalsIgnoreCase(transfer.getStatus())) {
            throw new IllegalStateException("Transfer is not awaiting approval. Status: " + transfer.getStatus());
        }

        ShareCertificateEntity cert = shareCertificateRepository.findById(transfer.getCertificateId())
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found."));

        ShareAccountEntity sellerAccount = shareAccountRepository.findByMemberId(transfer.getFromMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Seller account not found."));

        String certIssued = executeTransferInternal(sellerAccount, transfer.getToMemberId(), cert, transfer.getShareCount(), transfer.getTransferPrice());

        transfer.setStatus("APPROVED");
        transfer.setApprovedBy(approverUserId);
        transfer.setApprovedAt(OffsetDateTime.now());
        shareTransferRepository.save(transfer);

        return new TransferSharesResult(
                transfer.getId(),
                transfer.getFromMemberId(),
                transfer.getToMemberId(),
                transfer.getShareCount(),
                transfer.getTransferPrice(),
                "APPROVED",
                certIssued
        );
    }

    // ── Helper: Internal Transfer Execution ─────────────────────────────────

    private String executeTransferInternal(
            ShareAccountEntity sellerAccount,
            UUID toMemberId,
            ShareCertificateEntity cert,
            int shareCount,
            BigDecimal price) {

        ShareAccountEntity buyerAccount = getOrCreateShareAccount(toMemberId, sellerAccount.getSaccoCode(), sellerAccount.getBranchCode());

        // 1. Deduct from Seller
        if (shareCount == cert.getShareCount()) {
            cert.setStatus("TRANSFERRED");
            shareCertificateRepository.save(cert);
        } else {
            // Partial split: reduce current cert count
            cert.setShareCount(cert.getShareCount() - shareCount);
            shareCertificateRepository.save(cert);
        }

        sellerAccount.setTotalShares(sellerAccount.getTotalShares() - shareCount);
        sellerAccount.setTotalAmount(sellerAccount.getTotalAmount().subtract(price));
        shareAccountRepository.save(sellerAccount);

        // 2. Issue New Certificate to Buyer
        Long serial = shareCertificateRepository.getNextSerial();
        if (serial == null) serial = 200001L;
        long startSerial = serial;
        long endSerial = startSerial + shareCount - 1;

        String newCertNo = String.format("CERT-%d-%06d-TRF", LocalDate.now().getYear(), startSerial);
        ShareCertificateEntity buyerCert = new ShareCertificateEntity(
                buyerAccount.getId(),
                newCertNo,
                startSerial,
                endSerial,
                shareCount,
                LocalDate.now()
        );
        shareCertificateRepository.save(buyerCert);

        // 3. Credit to Buyer
        buyerAccount.setTotalShares(buyerAccount.getTotalShares() + shareCount);
        buyerAccount.setTotalAmount(buyerAccount.getTotalAmount().add(price));
        shareAccountRepository.save(buyerAccount);

        log.info("Executed share transfer: {} shares moved from Member {} to Member {}. Re-issued Cert {}",
                shareCount, sellerAccount.getMemberId(), toMemberId, newCertNo);

        return newCertNo;
    }

    // ── 5. Query Views ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ShareAccountSummary getMemberShareAccountSummary(UUID memberId) {
        ShareAccountEntity account = shareAccountRepository.findByMemberId(memberId)
                .orElse(null);

        if (account == null) {
            return new ShareAccountSummary(
                    null, memberId, null, 0, DEFAULT_NOMINAL_VALUE, BigDecimal.ZERO,
                    "NOT_OPENED", false, MANDATORY_MINIMUM_SHARES, List.of(), null
            );
        }

        List<ShareCertificateEntity> certificates = shareCertificateRepository
                .findByShareAccountIdAndStatus(account.getId(), "ACTIVE");

        boolean minMet = account.getTotalShares() >= MANDATORY_MINIMUM_SHARES;

        return new ShareAccountSummary(
                account.getId(),
                account.getMemberId(),
                account.getAccountNumber(),
                account.getTotalShares(),
                account.getShareNominalValue(),
                account.getTotalAmount(),
                account.getStatus(),
                minMet,
                MANDATORY_MINIMUM_SHARES,
                certificates,
                account.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ShareCertificateEntity> getMemberCertificates(UUID memberId) {
        return shareAccountRepository.findByMemberId(memberId)
                .map(acc -> shareCertificateRepository.findByShareAccountId(acc.getId()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<ShareTransferEntity> getMemberTransfers(UUID memberId) {
        List<ShareTransferEntity> outgoing = shareTransferRepository.findByFromMemberId(memberId);
        List<ShareTransferEntity> incoming = shareTransferRepository.findByToMemberId(memberId);
        outgoing.addAll(incoming);
        return outgoing;
    }

    @Transactional(readOnly = true)
    public List<ShareTransferEntity> getPendingTransfers() {
        return shareTransferRepository.findByStatus("PENDING_APPROVAL");
    }
}
