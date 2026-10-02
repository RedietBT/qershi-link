package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.ShareAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Share Account persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ShareAccountRepositoryPort {

    ShareAccount save(ShareAccount shareAccount);

    Optional<ShareAccount> findById(UUID id);

    Optional<ShareAccount> findByMemberId(UUID memberId);

    Optional<ShareAccount> findByAccountNumber(String accountNumber);

    List<ShareAccount> findByStatus(String status);

    List<ShareAccount> findAllActive();

    boolean existsByMemberId(UUID memberId);
}
