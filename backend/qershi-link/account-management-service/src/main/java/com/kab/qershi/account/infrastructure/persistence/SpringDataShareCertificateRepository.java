package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Share Certificates.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataShareCertificateRepository extends JpaRepository<ShareCertificateEntity, UUID> {

    List<ShareCertificateEntity> findByShareAccountId(UUID shareAccountId);

    List<ShareCertificateEntity> findByShareAccountIdAndStatus(UUID shareAccountId, String status);

    Optional<ShareCertificateEntity> findByCertificateNumber(String certificateNumber);

    @Query(value = "SELECT nextval('share_certificate_serial_seq')", nativeQuery = true)
    Long getNextSerial();

    long countByShareAccountIdAndStatus(UUID shareAccountId, String status);
}
