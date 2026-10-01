package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.infrastructure.persistence.AccountAuditLogEntity;
import com.kab.qershi.account.infrastructure.persistence.AccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountAuditLogRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountRepository;
import com.kab.qershi.account.infrastructure.rest.dto.KycApprovalRequest;
import com.kab.qershi.account.infrastructure.rest.dto.KycReactivationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AccountDormancyService Lifecycle & KYC Reactivation Tests")
class AccountDormancyServiceTest {

    private SpringDataAccountRepository accountRepository;
    private SpringDataAccountAuditLogRepository auditLogRepository;
    private AccountDormancyService dormancyService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(SpringDataAccountRepository.class);
        auditLogRepository = mock(SpringDataAccountAuditLogRepository.class);
        dormancyService = new AccountDormancyService(accountRepository, auditLogRepository);
    }

    @Test
    @DisplayName("Should sweep inactive accounts over 180 days and mark them DORMANT with audit trail")
    void shouldSweepInactiveAccountsToDormant() {
        LocalDate businessDate = LocalDate.of(2026, 10, 1);
        LocalDate inactiveDate = businessDate.minusDays(200);

        AccountEntity account = new AccountEntity();
        account.setAccountId(UUID.randomUUID());
        account.setAccountNo("0001-001-101-0001234");
        account.setUserId(UUID.randomUUID());
        account.setStatus(AccountStatus.ACTIVE);
        account.setLastActivityDate(inactiveDate);

        when(accountRepository.findDormantCandidates(eq(AccountStatus.ACTIVE), any(LocalDate.class), any(LocalDateTime.class)))
                .thenReturn(List.of(account));
        when(accountRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        int swept = dormancyService.sweepDormantAccounts(businessDate);

        assertEquals(1, swept);
        assertEquals(AccountStatus.DORMANT, account.getStatus());
        assertEquals(businessDate, account.getDormancyDate());
        assertEquals("NONE", account.getReactivationStatus());

        verify(auditLogRepository, times(1)).save(any(AccountAuditLogEntity.class));
        verify(accountRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should initiate KYC reactivation request by maker")
    void shouldInitiateKycReactivationByMaker() {
        String accountNo = "0001-001-101-0001234";
        UUID makerId = UUID.randomUUID();

        AccountEntity account = new AccountEntity();
        account.setAccountNo(accountNo);
        account.setUserId(UUID.randomUUID());
        account.setStatus(AccountStatus.DORMANT);
        account.setDormancyDate(LocalDate.now().minusDays(30));

        when(accountRepository.findByAccountNo(accountNo)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        KycReactivationRequest req = new KycReactivationRequest(
                "Member returned from assignment",
                "Kebele ID #8921 verified in-person, signature matches card"
        );

        AccountEntity result = dormancyService.initiateKycReactivation(accountNo, makerId, req);

        assertEquals("PENDING_CHECKER_APPROVAL", result.getReactivationStatus());
        assertEquals(makerId, result.getReactivationMakerUserId());
        assertTrue(result.getReactivationMakerNotes().contains("Kebele ID #8921"));

        verify(auditLogRepository, times(1)).save(any(AccountAuditLogEntity.class));
    }

    @Test
    @DisplayName("Should prevent maker from approving their own KYC reactivation request (Anti-Self-Approval)")
    void shouldBlockSelfApprovalForReactivation() {
        String accountNo = "0001-001-101-0001234";
        UUID operatorId = UUID.randomUUID();

        AccountEntity account = new AccountEntity();
        account.setAccountNo(accountNo);
        account.setStatus(AccountStatus.DORMANT);
        account.setReactivationStatus("PENDING_CHECKER_APPROVAL");
        account.setReactivationMakerUserId(operatorId); // Same operator

        when(accountRepository.findByAccountNo(accountNo)).thenReturn(Optional.of(account));

        KycApprovalRequest approvalReq = new KycApprovalRequest("Attempting self approval");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                dormancyService.approveKycReactivation(accountNo, operatorId, approvalReq)
        );

        assertTrue(ex.getMessage().contains("Four-Eye Anti-Self-Approval Violation"));
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    @DisplayName("Should successfully approve KYC reactivation by distinct checker supervisor and restore ACTIVE status")
    void shouldApproveReactivationBySupervisor() {
        String accountNo = "0001-001-101-0001234";
        UUID makerId = UUID.randomUUID();
        UUID checkerId = UUID.randomUUID();

        AccountEntity account = new AccountEntity();
        account.setAccountNo(accountNo);
        account.setUserId(UUID.randomUUID());
        account.setStatus(AccountStatus.DORMANT);
        account.setDormancyDate(LocalDate.now().minusDays(45));
        account.setReactivationStatus("PENDING_CHECKER_APPROVAL");
        account.setReactivationMakerUserId(makerId);

        when(accountRepository.findByAccountNo(accountNo)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        KycApprovalRequest approvalReq = new KycApprovalRequest("Physical biometric card audited and confirmed.");

        AccountEntity approved = dormancyService.approveKycReactivation(accountNo, checkerId, approvalReq);

        assertEquals(AccountStatus.ACTIVE, approved.getStatus());
        assertNull(approved.getDormancyDate());
        assertEquals("APPROVED", approved.getReactivationStatus());
        assertEquals(checkerId, approved.getReactivationCheckerUserId());
        assertNotNull(approved.getReactivatedAt());
        assertEquals(LocalDate.now(), approved.getLastActivityDate());

        verify(auditLogRepository, times(1)).save(any(AccountAuditLogEntity.class));
    }
}
