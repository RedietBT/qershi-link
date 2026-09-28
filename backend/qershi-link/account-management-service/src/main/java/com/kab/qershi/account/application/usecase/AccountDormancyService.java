package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.infrastructure.persistence.AccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Account Dormancy Lifecycle Service.
 * Automatically flags active accounts with no operational activity over 180 days as DORMANT.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class AccountDormancyService {

    private static final Logger log = LoggerFactory.getLogger(AccountDormancyService.class);
    private static final int DORMANCY_THRESHOLD_DAYS = 180;

    private final SpringDataAccountRepository accountRepository;

    public AccountDormancyService(SpringDataAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public int sweepDormantAccounts(LocalDate businessDate) {
        LocalDate cutoffDate = businessDate.minusDays(DORMANCY_THRESHOLD_DAYS);
        log.info("Running Account Dormancy Sweep for business date: {}, cutoffDate: {}", businessDate, cutoffDate);

        List<AccountEntity> dormantCandidates = accountRepository.findByStatusAndLastActivityDateBefore(
                AccountStatus.ACTIVE, cutoffDate
        );

        if (dormantCandidates.isEmpty()) {
            log.info("Dormancy sweep finished: No accounts exceeded 180 days inactivity.");
            return 0;
        }

        for (AccountEntity account : dormantCandidates) {
            account.setStatus(AccountStatus.DORMANT);
            account.setUpdatedAt(LocalDateTime.now());
            log.warn("Account {} marked DORMANT due to inactivity since {}",
                    account.getAccountNo(), account.getLastActivityDate());
        }

        accountRepository.saveAll(dormantCandidates);
        log.info("Successfully transitioned {} accounts to DORMANT status.", dormantCandidates.size());
        return dormantCandidates.size();
    }
}
