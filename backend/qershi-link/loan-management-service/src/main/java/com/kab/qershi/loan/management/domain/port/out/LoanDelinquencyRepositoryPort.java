package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanDelinquencySnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Repository Port for LoanDelinquencySnapshot persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanDelinquencyRepositoryPort {

    List<LoanDelinquencySnapshot> saveAll(List<LoanDelinquencySnapshot> snapshots);

    List<LoanDelinquencySnapshot> findByBusinessDate(LocalDate businessDate);

    Optional<LoanDelinquencySnapshot> findByAccountIdAndBusinessDate(UUID accountId, LocalDate businessDate);

    List<LoanDelinquencySnapshot> findLatestSnapshots();

    List<LoanDelinquencySnapshot> findLatestDelinquentSnapshots();
}
