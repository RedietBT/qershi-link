package com.kab.qershi.profile.domain.ports.outbound;

import com.kab.qershi.profile.domain.model.NextOfKin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port interface defining persistence contracts for nominated beneficiary (Next of Kin) records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NextOfKinRepositoryPort {

    NextOfKin saveNextOfKin(NextOfKin nextOfKin);

    Optional<NextOfKin> findById(UUID kinId);

    List<NextOfKin> findByUserId(UUID userId);

    void deleteById(UUID kinId);

    void deleteByUserId(UUID userId);
}
