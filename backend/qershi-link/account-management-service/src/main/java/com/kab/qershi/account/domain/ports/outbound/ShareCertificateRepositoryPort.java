package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.ShareCertificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Share Certificate persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ShareCertificateRepositoryPort {

    ShareCertificate save(ShareCertificate certificate);

    Optional<ShareCertificate> findById(UUID id);

    List<ShareCertificate> findByShareAccountId(UUID shareAccountId);

    List<ShareCertificate> findActiveByShareAccountId(UUID shareAccountId);

    Optional<ShareCertificate> findByCertificateNumber(String certificateNumber);

    Long getNextSerial();
}
