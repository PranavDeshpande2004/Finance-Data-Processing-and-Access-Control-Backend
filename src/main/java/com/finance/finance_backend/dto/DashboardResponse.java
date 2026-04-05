package com.finance.finance_backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//Response DTO for the dashboard summary data

@Getter
@Builder
public class DashboardResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String,BigDecimal> categoryBreakdown;
    private List<MonthlyTrend> monthlyTrends;
    private List<TransactionResponse>recentTransactions;

    @Getter
    @Builder
    public static class  MonthlyTrend{
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
    }


}
