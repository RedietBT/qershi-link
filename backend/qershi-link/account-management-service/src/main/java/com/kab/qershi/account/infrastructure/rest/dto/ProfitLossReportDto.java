package com.kab.qershi.account.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Profit and Loss (Income Statement) Financial Statement DTO.
 * Computes: Operating Income - Operating Expenses = Net Surplus (Profit/Deficit).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ProfitLossReportDto {

    private LocalDate startDate;
    private LocalDate endDate;

    private List<ReportSectionLineDto> operatingIncome;
    private BigDecimal totalOperatingIncome;

    private List<ReportSectionLineDto> operatingExpenses;
    private BigDecimal totalOperatingExpenses;

    private BigDecimal netSurplus;
    private String status; // "NET_SURPLUS" or "NET_DEFICIT"

    public ProfitLossReportDto() {}

    public ProfitLossReportDto(LocalDate startDate, LocalDate endDate,
                               List<ReportSectionLineDto> operatingIncome, BigDecimal totalOperatingIncome,
                               List<ReportSectionLineDto> operatingExpenses, BigDecimal totalOperatingExpenses,
                               BigDecimal netSurplus, String status) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.operatingIncome = operatingIncome;
        this.totalOperatingIncome = totalOperatingIncome;
        this.operatingExpenses = operatingExpenses;
        this.totalOperatingExpenses = totalOperatingExpenses;
        this.netSurplus = netSurplus;
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<ReportSectionLineDto> getOperatingIncome() {
        return operatingIncome;
    }

    public void setOperatingIncome(List<ReportSectionLineDto> operatingIncome) {
        this.operatingIncome = operatingIncome;
    }

    public BigDecimal getTotalOperatingIncome() {
        return totalOperatingIncome;
    }

    public void setTotalOperatingIncome(BigDecimal totalOperatingIncome) {
        this.totalOperatingIncome = totalOperatingIncome;
    }

    public List<ReportSectionLineDto> getOperatingExpenses() {
        return operatingExpenses;
    }

    public void setOperatingExpenses(List<ReportSectionLineDto> operatingExpenses) {
        this.operatingExpenses = operatingExpenses;
    }

    public BigDecimal getTotalOperatingExpenses() {
        return totalOperatingExpenses;
    }

    public void setTotalOperatingExpenses(BigDecimal totalOperatingExpenses) {
        this.totalOperatingExpenses = totalOperatingExpenses;
    }

    public BigDecimal getNetSurplus() {
        return netSurplus;
    }

    public void setNetSurplus(BigDecimal netSurplus) {
        this.netSurplus = netSurplus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
