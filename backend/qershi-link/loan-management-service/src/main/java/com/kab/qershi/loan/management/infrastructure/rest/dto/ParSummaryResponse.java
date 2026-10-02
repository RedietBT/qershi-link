package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.ParSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Summary metrics for Portfolio at Risk (PAR) and regulatory delinquency reserve provisions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Aggregated Portfolio at Risk (PAR) metrics and regulatory provisioning reserves")
public record ParSummaryResponse(
        @Schema(description = "Total number of evaluated active loans")
        int totalLoans,

        @Schema(description = "Total outstanding portfolio principal (ETB)")
        BigDecimal totalOutstandingPrincipal,

        @Schema(description = "Number of healthy current loans (0 DPD)")
        int currentCount,

        @Schema(description = "Outstanding balance of current loans (ETB)")
        BigDecimal currentAmount,

        @Schema(description = "Number of watchlist loans (1–30 DPD)")
        int par30Count,

        @Schema(description = "Outstanding balance of PAR 1–30 loans (ETB)")
        BigDecimal par30Amount,

        @Schema(description = "Number of substandard loans (31–60 DPD)")
        int par60Count,

        @Schema(description = "Outstanding balance of PAR 31–60 loans (ETB)")
        BigDecimal par60Amount,

        @Schema(description = "Number of doubtful loans (61–90 DPD)")
        int par90Count,

        @Schema(description = "Outstanding balance of PAR 61–90 loans (ETB)")
        BigDecimal par90Amount,

        @Schema(description = "Number of loss / NPL loans (>90 DPD)")
        int lossCount,

        @Schema(description = "Outstanding balance of loss / NPL loans (ETB)")
        BigDecimal lossAmount,

        @Schema(description = "Non-Performing Loan ratio percentage (PAR 90+ / Total)")
        BigDecimal nplRatioPct,

        @Schema(description = "Total required regulatory loan loss provision reserve (ETB)")
        BigDecimal totalProvisionReserve
) {
    public static ParSummaryResponse fromDomain(ParSummary domain) {
        if (domain == null) return null;
        return new ParSummaryResponse(
                domain.totalLoans(),
                domain.totalPrincipal(),
                domain.currentCount(),
                domain.currentAmount(),
                domain.par30Count(),
                domain.par30Amount(),
                domain.par60Count(),
                domain.par60Amount(),
                domain.par90Count(),
                domain.par90Amount(),
                domain.lossCount(),
                domain.lossAmount(),
                domain.nplRatio(),
                domain.totalProvisions()
        );
    }
}
