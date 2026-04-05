package com.zorvyn.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> categoryTotals;
    private List<FinancialRecordDto.Response> recentActivity;
    private List<MonthlyTrendDto> monthlyTrends;

    @Data
    @Builder
    public static class MonthlyTrendDto {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
    }
}
