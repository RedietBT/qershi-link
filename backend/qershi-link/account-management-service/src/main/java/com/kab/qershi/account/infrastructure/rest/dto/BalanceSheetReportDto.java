package com.kab.qershi.account.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Balance Sheet Financial Statement DTO.
 * Evaluates the fundamental accounting equation: Assets = Liabilities + Equity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class BalanceSheetReportDto {

    private LocalDate asOfDate;
    private List<ReportSectionLineDto> assets;
    private BigDecimal totalAssets;

    private List<ReportSectionLineDto> liabilities;
    private BigDecimal totalLiabilities;

    private List<ReportSectionLineDto> equity;
    private BigDecimal totalEquity;

    private BigDecimal currentPeriodSurplus;
    private BigDecimal totalLiabilitiesAndEquity;
    private Boolean isBalanced;
    private BigDecimal variance;

    public BalanceSheetReportDto() {}

    public BalanceSheetReportDto(LocalDate asOfDate,
                                 List<ReportSectionLineDto> assets, BigDecimal totalAssets,
                                 List<ReportSectionLineDto> liabilities, BigDecimal totalLiabilities,
                                 List<ReportSectionLineDto> equity, BigDecimal totalEquity,
                                 BigDecimal currentPeriodSurplus, BigDecimal totalLiabilitiesAndEquity,
                                 Boolean isBalanced, BigDecimal variance) {
        this.asOfDate = asOfDate;
        this.assets = assets;
        this.totalAssets = totalAssets;
        this.liabilities = liabilities;
        this.totalLiabilities = totalLiabilities;
        this.equity = equity;
        this.totalEquity = totalEquity;
        this.currentPeriodSurplus = currentPeriodSurplus;
        this.totalLiabilitiesAndEquity = totalLiabilitiesAndEquity;
        this.isBalanced = isBalanced;
        this.variance = variance;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public List<ReportSectionLineDto> getAssets() {
        return assets;
    }

    public void setAssets(List<ReportSectionLineDto> assets) {
        this.assets = assets;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public List<ReportSectionLineDto> getLiabilities() {
        return liabilities;
    }

    public void setLiabilities(List<ReportSectionLineDto> liabilities) {
        this.liabilities = liabilities;
    }

    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public List<ReportSectionLineDto> getEquity() {
        return equity;
    }

    public void setEquity(List<ReportSectionLineDto> equity) {
        this.equity = equity;
    }

    public BigDecimal getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(BigDecimal totalEquity) {
        this.totalEquity = totalEquity;
    }

    public BigDecimal getCurrentPeriodSurplus() {
        return currentPeriodSurplus;
    }

    public void setCurrentPeriodSurplus(BigDecimal currentPeriodSurplus) {
        this.currentPeriodSurplus = currentPeriodSurplus;
    }

    public BigDecimal getTotalLiabilitiesAndEquity() {
        return totalLiabilitiesAndEquity;
    }

    public void setTotalLiabilitiesAndEquity(BigDecimal totalLiabilitiesAndEquity) {
        this.totalLiabilitiesAndEquity = totalLiabilitiesAndEquity;
    }

    public Boolean getIsBalanced() {
        return isBalanced;
    }

    public void setIsBalanced(Boolean balanced) {
        isBalanced = balanced;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }
}
