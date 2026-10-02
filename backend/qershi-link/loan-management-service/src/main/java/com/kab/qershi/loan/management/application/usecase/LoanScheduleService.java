package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanAccountGuarantor;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.in.LoanScheduleUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.LoanGuarantorRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.RepaymentScheduleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic service managing Loan Account, Schedule & Guarantor queries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class LoanScheduleService implements LoanScheduleUseCase {

    private final LoanAccountRepositoryPort accountRepository;
    private final RepaymentScheduleRepositoryPort scheduleRepository;
    private final LoanGuarantorRepositoryPort guarantorRepository;

    public LoanScheduleService(LoanAccountRepositoryPort accountRepository,
                               RepaymentScheduleRepositoryPort scheduleRepository,
                               LoanGuarantorRepositoryPort guarantorRepository) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.guarantorRepository = guarantorRepository;
    }

    @Override
    public LoanAccount getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found with ID: " + accountId));
    }

    @Override
    public List<RepaymentSchedule> getAccountSchedule(UUID accountId) {
        return scheduleRepository.findByAccountIdOrderByInstallmentNoAsc(accountId);
    }

    @Override
    public List<LoanAccount> getUserAccounts(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public List<LoanAccountGuarantor> getAccountGuarantors(UUID accountId) {
        return guarantorRepository.findByAccountId(accountId);
    }
}
