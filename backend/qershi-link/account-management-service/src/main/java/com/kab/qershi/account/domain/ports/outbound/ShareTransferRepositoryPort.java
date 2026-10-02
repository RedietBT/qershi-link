package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.ShareTransfer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Share Transfer persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ShareTransferRepositoryPort {

    ShareTransfer save(ShareTransfer transfer);

    Optional<ShareTransfer> findById(UUID id);

    List<ShareTransfer> findByFromMemberId(UUID fromMemberId);

    List<ShareTransfer> findByToMemberId(UUID toMemberId);

    List<ShareTransfer> findByStatus(String status);
}
