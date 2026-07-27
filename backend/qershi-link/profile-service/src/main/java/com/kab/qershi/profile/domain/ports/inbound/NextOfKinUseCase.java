package com.kab.qershi.profile.domain.ports.inbound;

import com.kab.qershi.profile.domain.model.NextOfKin;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Port interface defining use cases for nominated beneficiary (Next of Kin) records and allocation matrices.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface NextOfKinUseCase {

    NextOfKin addNextOfKin(
            UUID userId,
            String fullName,
            String relationship,
            String primaryPhone,
            String idNumber,
            String physicalAddress,
            BigDecimal allocationPercentage
    );

    List<NextOfKin> getNextOfKinByUserId(UUID userId);

    NextOfKin updateNextOfKin(
            UUID kinId,
            String fullName,
            String relationship,
            String primaryPhone,
            String idNumber,
            String physicalAddress,
            BigDecimal allocationPercentage
    );

    void deleteNextOfKin(UUID kinId);
}
