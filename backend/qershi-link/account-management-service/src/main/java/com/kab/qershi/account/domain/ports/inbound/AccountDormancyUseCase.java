package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.infrastructure.rest.dto.KycApprovalRequest;
import com.kab.qershi.account.infrastructure.rest.dto.KycReactivationRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Inbound use case port for managing account dormancy sweeps and Four-Eye KYC reactivation workflows.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountDormancyUseCase {

    int sweepDormantAccounts(LocalDate businessDate);

    Account initiateKycReactivation(String accountNo, UUID makerUserId, KycReactivationRequest request);

    Account approveKycReactivation(String accountNo, UUID checkerUserId, KycApprovalRequest request);

    Account rejectKycReactivation(String accountNo, UUID checkerUserId, KycApprovalRequest request);

    List<Account> getDormantAccounts();

    List<Account> getPendingReactivations();
}
