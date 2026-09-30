package com.kab.qershi.account.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Complete Trial Balance Report DTO.
 * Verifies that Total Debits == Total Credits across the entire General Ledger.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TrialBalanceReportDto {

    private LocalDate asOfDate;
    private List<TrialBalanceLineDto> lines;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private Boolean isBalanced;
    private BigDecimal variance;

    public TrialBalanceReportDto() {}

    public TrialBalanceReportDto(LocalDate asOfDate, List<TrialBalanceLineDto> lines,
                                 BigDecimal totalDebits, BigDecimal totalCredits,
                                 Boolean isBalanced, BigDecimal variance) {
        this.asOfDate = asOfDate;
        this.lines = lines;
        this.totalDebits = totalDebits;
        this.totalCredits = totalCredits;
        this.isBalanced = isBalanced;
        this.variance = variance;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public List<TrialBalanceLineDto> getLines() {
        return lines;
    }

    public void setLines(List<TrialBalanceLineDto> lines) {
        this.lines = lines;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public void setTotalDebits(BigDecimal totalDebits) {
        this.totalDebits = totalDebits;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
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
