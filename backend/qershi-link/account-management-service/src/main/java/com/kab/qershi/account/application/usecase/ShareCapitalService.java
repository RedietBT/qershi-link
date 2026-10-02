package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.ShareAccount;
import com.kab.qershi.account.domain.model.ShareCertificate;
import com.kab.qershi.account.domain.model.ShareTransfer;
import com.kab.qershi.account.domain.ports.inbound.ShareCapitalUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ShareAccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ShareCertificateRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ShareTransferRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Pure Hexagonal Application Service driving Share Capital operations.
 * Operates purely on Domain Models and Ports (zero direct persistence coupling).
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Service
public class ShareCapitalService implements ShareCapitalUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShareCapitalService.class);

    private final ShareAccountRepositoryPort shareAccountRepository;
    private final ShareCertificateRepositoryPort shareCertificateRepository;
    private final ShareTransferRepositoryPort shareTransferRepository;
    private final AccountRepositoryPort accountRepository;

    public ShareCapitalService(
            ShareAccountRepositoryPort shareAccountRepository,
            ShareCertificateRepositoryPort shareCertificateRepository,
            ShareTransferRepositoryPort shareTransferRepository,
            AccountRepositoryPort accountRepository) {
        this.shareAccountRepository = shareAccountRepository;
        this.shareCertificateRepository = shareCertificateRepository;
        this.shareTransferRepository = shareTransferRepository;
        this.accountRepository = accountRepository;
    }

    // ── 1. Open or Get Share Account ─────────────────────────────────────────

    @Override
    @Transactional
    public ShareAccount getOrCreateShareAccount(UUID memberId, String saccoCode, String branchCode) {
        return shareAccountRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    String cleanSacco = (saccoCode != null && !saccoCode.isBlank()) ? saccoCode.toUpperCase() : "SACCO";
                    String accNo = String.format("SHR-%s-%s", cleanSacco, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    ShareAccount newAccount = ShareAccount.createNew(
                            memberId, accNo, ShareAccount.DEFAULT_NOMINAL_VALUE, saccoCode, branchCode
                    );
                    log.info("Opened new Share Capital Account {} for member {}", accNo, memberId);
                    return shareAccountRepository.save(newAccount);
                });
    }

    // ── 2. Purchase Shares ───────────────────────────────────────────────────

    @Override
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

        ShareAccount shareAccount = getOrCreateShareAccount(memberId, saccoCode, branchCode);
        BigDecimal totalCost = shareAccount.getShareNominalValue().multiply(BigDecimal.valueOf(shareCount));

        // 1. Debit Source Account if specified
        String glDebitAccount = "GL 1110 (Cash on Hand)";
        if (sourceAccountNo != null && !sourceAccountNo.isBlank()) {
            Account sourceAccount = accountRepository.findByAccountNo(sourceAccountNo)
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
        ShareCertificate certificate = new ShareCertificate(
                UUID.randomUUID(),
                shareAccount.getId(),
                certNumber,
                startSerial,
                endSerial,
                shareCount,
                LocalDate.now(),
                "ACTIVE",
                OffsetDateTime.now()
        );
        shareCertificateRepository.save(certificate);

        // 4. Update Share Account totals via Domain Method
        shareAccount.creditShares(shareCount, totalCost);
        shareAccountRepository.save(shareAccount);

        // 5. Audit GL Double-Entry reference
        String glJournalRef = String.format("GL-SHR-%d-%s", currentYear, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        log.info("Posted Double-Entry GL Journal {}: DEBIT [{}] {} ETB | CREDIT [GL 3100 Member Share Capital] {} ETB",
                glJournalRef, glDebitAccount, totalCost, totalCost);

        return new PurchaseSharesResult(
                shareAccount.getId(),
                shareAccount.getAccountNumber(),
                certNumber,
                startSerial,
                endSerial,
                shareCount,
                totalCost,
                shareAccount.getTotalShares(),
                shareAccount.getTotalAmount(),
                glJournalRef,
                shareAccount.isMandatoryRequirementMet()
        );
    }

    // ── 3. Peer-to-Peer Share Transfer ───────────────────────────────────────

    @Override
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

        ShareCertificate certificate = shareCertificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Share certificate not found: " + certificateId));

        if (!certificate.isActive()) {
            throw new IllegalStateException("Certificate is not active for transfer. Current status: " + certificate.getStatus());
        }

        ShareAccount sellerAccount = shareAccountRepository.findByMemberId(fromMemberId)
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

        ShareTransfer transfer = new ShareTransfer(
                UUID.randomUUID(),
                fromMemberId,
                toMemberId,
                certificateId,
                shareCount,
                price,
                "PENDING_APPROVAL",
                null,
                null,
                null,
                OffsetDateTime.now()
        );

        String certIssued = null;

        if (autoApprove) {
            transfer.approve(approvedBy != null ? approvedBy : fromMemberId);
            certIssued = executeTransferInternal(sellerAccount, toMemberId, certificate, shareCount, price);
        }

        ShareTransfer savedTransfer = shareTransferRepository.save(transfer);

        return new TransferSharesResult(
                savedTransfer.getId(),
                fromMemberId,
                toMemberId,
                shareCount,
                price,
                savedTransfer.getStatus(),
                certIssued
        );
    }

    // ── 4. Approve Pending Transfer ──────────────────────────────────────────

    @Override
    @Transactional
    public TransferSharesResult approveTransfer(UUID transferId, UUID approverUserId) {
        ShareTransfer transfer = shareTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer record not found: " + transferId));

        if (!"PENDING_APPROVAL".equalsIgnoreCase(transfer.getStatus())) {
            throw new IllegalStateException("Transfer is not awaiting approval. Status: " + transfer.getStatus());
        }

        ShareCertificate cert = shareCertificateRepository.findById(transfer.getCertificateId())
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found."));

        ShareAccount sellerAccount = shareAccountRepository.findByMemberId(transfer.getFromMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Seller account not found."));

        String certIssued = executeTransferInternal(sellerAccount, transfer.getToMemberId(), cert, transfer.getShareCount(), transfer.getTransferPrice());

        transfer.approve(approverUserId);
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
            ShareAccount sellerAccount,
            UUID toMemberId,
            ShareCertificate cert,
            int shareCount,
            BigDecimal price) {

        ShareAccount buyerAccount = getOrCreateShareAccount(toMemberId, sellerAccount.getSaccoCode(), sellerAccount.getBranchCode());

        // 1. Deduct from Seller via Domain logic
        if (shareCount == cert.getShareCount()) {
            cert.markTransferred();
            shareCertificateRepository.save(cert);
        } else {
            cert.reduceShares(shareCount);
            shareCertificateRepository.save(cert);
        }

        sellerAccount.debitShares(shareCount, price);
        shareAccountRepository.save(sellerAccount);

        // 2. Issue New Certificate to Buyer
        Long serial = shareCertificateRepository.getNextSerial();
        if (serial == null) serial = 200001L;
        long startSerial = serial;
        long endSerial = startSerial + shareCount - 1;

        String newCertNo = String.format("CERT-%d-%06d-TRF", LocalDate.now().getYear(), startSerial);
        ShareCertificate buyerCert = new ShareCertificate(
                UUID.randomUUID(),
                buyerAccount.getId(),
                newCertNo,
                startSerial,
                endSerial,
                shareCount,
                LocalDate.now(),
                "ACTIVE",
                OffsetDateTime.now()
        );
        shareCertificateRepository.save(buyerCert);

        // 3. Credit to Buyer via Domain logic
        buyerAccount.creditShares(shareCount, price);
        shareAccountRepository.save(buyerAccount);

        log.info("Executed share transfer: {} shares moved from Member {} to Member {}. Re-issued Cert {}",
                shareCount, sellerAccount.getMemberId(), toMemberId, newCertNo);

        return newCertNo;
    }

    // ── 5. Query Views ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ShareAccountSummary getMemberShareAccountSummary(UUID memberId) {
        ShareAccount account = shareAccountRepository.findByMemberId(memberId)
                .orElse(null);

        if (account == null) {
            return new ShareAccountSummary(
                    null, memberId, null, 0, ShareAccount.DEFAULT_NOMINAL_VALUE, BigDecimal.ZERO,
                    "NOT_OPENED", false, ShareAccount.MANDATORY_MINIMUM_SHARES, List.of()
            );
        }

        List<ShareCertificate> certificates = shareCertificateRepository
                .findActiveByShareAccountId(account.getId());

        return new ShareAccountSummary(
                account.getId(),
                account.getMemberId(),
                account.getAccountNumber(),
                account.getTotalShares(),
                account.getShareNominalValue(),
                account.getTotalAmount(),
                account.getStatus(),
                account.isMandatoryRequirementMet(),
                ShareAccount.MANDATORY_MINIMUM_SHARES,
                certificates
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareCertificate> getMemberCertificates(UUID memberId) {
        return shareAccountRepository.findByMemberId(memberId)
                .map(acc -> shareCertificateRepository.findByShareAccountId(acc.getId()))
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareTransfer> getMemberTransfers(UUID memberId) {
        List<ShareTransfer> outgoing = shareTransferRepository.findByFromMemberId(memberId);
        List<ShareTransfer> incoming = shareTransferRepository.findByToMemberId(memberId);
        outgoing.addAll(incoming);
        return outgoing;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareTransfer> getPendingTransfers() {
        return shareTransferRepository.findByStatus("PENDING_APPROVAL");
    }

    @Override
    @Transactional(readOnly = true)
    public ShareAccountSummary getShareAccountByPhone(String phoneNumber) {
        List<com.kab.qershi.account.domain.model.Account> accounts = accountRepository.findByPhoneNumber(phoneNumber);
        if (accounts.isEmpty()) {
            throw new IllegalArgumentException("No member account found for phone number: " + phoneNumber);
        }
        // Resolve memberId from the savings account's userId
        UUID memberId = accounts.get(0).getUserId();
        return getMemberShareAccountSummary(memberId);
    }
}
