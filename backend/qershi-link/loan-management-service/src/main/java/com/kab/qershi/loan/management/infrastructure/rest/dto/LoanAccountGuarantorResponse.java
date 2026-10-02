package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.LoanAccountGuarantor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for SACCO Loan Account Peer Guarantors.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanAccountGuarantorResponse(
        UUID guarantorId,
        UUID accountId,
        UUID applicationId,
        UUID guarantorUserId,
        String guarantorName,
        String guarantorPhone,
        String savingsAccountNo,
        BigDecimal guaranteedAmount,
        UUID lienId,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static LoanAccountGuarantorResponse fromDomain(LoanAccountGuarantor domain) {
        if (domain == null) return null;
        return new LoanAccountGuarantorResponse(
                domain.getGuarantorId(),
                domain.getAccountId(),
                domain.getApplicationId(),
                domain.getGuarantorUserId(),
                domain.getGuarantorName(),
                domain.getGuarantorPhone(),
                domain.getSavingsAccountNo(),
                domain.getGuaranteedAmount(),
                domain.getLienId(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
