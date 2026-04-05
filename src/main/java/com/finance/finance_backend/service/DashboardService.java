//package com.finance.finance_backend.service;
//
//import com.finance.finance_backend.dto.DashboardResponse;
//import com.finance.finance_backend.dto.DashboardResponse.MonthlyTrend;
//import com.finance.finance_backend.dto.TransactionResponse;
//import com.finance.finance_backend.entity.Transaction;
//import com.finance.finance_backend.enums.TransactionType;
//import com.finance.finance_backend.repository.TransactionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * Service for generating dashboard summary and analytics data.
// * All methods aggregate non-deleted transaction records only.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DashboardService {
//
//    private static final int RECENT_TRANSACTION_LIMIT = 10;
//
//
//    private final TransactionRepository transactionRepository;
//
//    /**
//     * Builds the complete dashboard summary including:
//     * - Total income, expenses, and net balance
//     * - Category-wise breakdown
//     * - Monthly trends
//     * - Recent 10 transactions
//     *
//     * @return DashboardResponse DTO
//     */
//    @Transactional(readOnly = true)
//    public DashboardResponse getDashboardSummary() {
//        log.info("Building dashboard summary");
//
//        BigDecimal totalIncome   = getTotalByType(TransactionType.INCOME);
//        BigDecimal totalExpenses = getTotalByType(TransactionType.EXPENSE);
//        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);
//
//        Map<String, BigDecimal> categoryBreakdown = getCategoryBreakdown();
//        List<MonthlyTrend>      monthlyTrends     = getMonthlyTrends();
//        List<TransactionResponse> recentActivity  = getRecentTransactions();
//
//        log.info("Dashboard summary built — income: {}, expenses: {}, net: {}",
//                totalIncome, totalExpenses, netBalance);
//
//        return DashboardResponse.builder()
//                .totalIncome(totalIncome)
//                .totalExpenses(totalExpenses)
//                .netBalance(netBalance)
//                .categoryBreakdown(categoryBreakdown)
//                .monthlyTrends(monthlyTrends)
//                .recentTransactions(recentActivity)
//                .build();
//    }
//
//    /**
//     * Returns total income or total expenses.
//     *
//     * @param type INCOME or EXPENSE
//     * @return sum as BigDecimal
//     */
//    @Transactional(readOnly = true)
//    public BigDecimal getTotalByType(TransactionType type) {
//        BigDecimal total = transactionRepository.sumByType(type);
//        return total != null ? total : BigDecimal.ZERO;
//    }
//
//    /**
//     * Returns total amount spent per category.
//     *
//     * @return map of category name to total amount
//     */
//    @Transactional(readOnly = true)
//    public Map<String, BigDecimal> getCategoryBreakdown() {
//        log.info("Building category breakdown");
//
//        List<Object[]> results =
//                transactionRepository.sumGroupedByCategory();
//
//        Map<String, BigDecimal> breakdown = new HashMap<>();
//        for (Object[] row : results) {
//            String     category = (String) row[0];
//            BigDecimal total    = (BigDecimal) row[1];
//            breakdown.put(category, total);
//        }
//        return breakdown;
//    }
//
//    /**
//     * Returns monthly income and expense totals grouped by month.
//     *
//     * @return list of MonthlyTrend DTOs
//     */
//    @Transactional(readOnly = true)
//    public List<MonthlyTrend> getMonthlyTrends() {
//        log.info("Building monthly trends");
//
//        List<Object[]> results = transactionRepository.monthlyTrends();
//
//        Map<String, MonthlyTrendBuilder> trendMap = new HashMap<>();
//
//        for (Object[] row : results) {
//            String          month  = (String) row[0];
//            TransactionType type   = TransactionType.valueOf((String) row[1]);
//            BigDecimal      amount = (BigDecimal) row[2];
//
//            trendMap.computeIfAbsent(month, MonthlyTrendBuilder::new)
//                    .add(type, amount);
//        }
//
//        return trendMap.values().stream()
//                .map(MonthlyTrendBuilder::build)
//                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Returns the 10 most recent non-deleted transactions.
//     *
//     * @return list of TransactionResponse DTOs
//     */
//    @Transactional(readOnly = true)
//    public List<TransactionResponse> getRecentTransactions() {
//        log.info("Fetching recent {} transactions", RECENT_TRANSACTION_LIMIT);
//
//        List<Transaction> transactions =
//                transactionRepository.findRecentTransactions(
//                        PageRequest.of(0, RECENT_TRANSACTION_LIMIT)
//                );
//
//        return transactions.stream()
//                .map(TransactionResponse::fromEntity)
//                .collect(Collectors.toList());
//    }
//
//
//
//    /**
//     * Internal builder to accumulate income and expense
//     * totals per month before building a MonthlyTrend DTO.
//     */
//    private static class MonthlyTrendBuilder {
//
//        private final String month;
//        private BigDecimal income  = BigDecimal.ZERO;
//        private BigDecimal expense = BigDecimal.ZERO;
//
//        MonthlyTrendBuilder(String month) {
//            this.month = month;
//        }
//
//        void add(TransactionType type, BigDecimal amount) {
//            if (type == TransactionType.INCOME) {
//                income = income.add(amount);
//            } else {
//                expense = expense.add(amount);
//            }
//        }
//
//        MonthlyTrend build() {
//            return MonthlyTrend.builder()
//                    .month(month)
//                    .income(income)
//                    .expense(expense)
//                    .build();
//        }
//    }
//}




package com.finance.finance_backend.service;

import com.finance.finance_backend.dto.DashboardResponse;
import com.finance.finance_backend.dto.DashboardResponse.MonthlyTrend;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for dashboard summary and analytics.
 * All methods aggregate non-deleted transactions only.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_LIMIT = 10;

    private final TransactionRepository transactionRepository;

    /**
     * Builds complete dashboard summary.
     * Includes totals, category breakdown,
     * monthly trends, and recent 10 transactions.
     *
     * @return DashboardResponse DTO
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary() {
        log.info("Building dashboard summary");

        BigDecimal totalIncome   = getTotalByType(TransactionType.INCOME);
        BigDecimal totalExpenses = getTotalByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        log.info("Income: {} | Expenses: {} | Net: {}",
                totalIncome, totalExpenses, netBalance);

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryBreakdown(getCategoryBreakdown())
                .monthlyTrends(getMonthlyTrends())
                .recentTransactions(getRecentTransactions())
                .build();
    }

    /**
     * Returns total INCOME or EXPENSE amount.
     * Returns ZERO if no records exist.
     *
     * @param type INCOME or EXPENSE
     * @return total as BigDecimal
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalByType(TransactionType type) {
        BigDecimal total = transactionRepository.sumByType(type);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Returns total amount grouped by category.
     *
     * @return map of category → total amount
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryBreakdown() {
        log.info("Building category breakdown");

        List<Object[]> results =
                transactionRepository.sumGroupedByCategory();

        Map<String, BigDecimal> breakdown = new HashMap<>();
        for (Object[] row : results) {
            String     category = (String)     row[0];
            BigDecimal total    = (BigDecimal) row[1];
            breakdown.put(category, total);
        }
        return breakdown;
    }

    /**
     * Returns monthly income and expense totals.
     *
     * Safely resolves TransactionType whether the JPQL
     * returns it as a String or as an Enum object —
     * fixes ClassCastException on both JPA implementations.
     *
     * @return list of MonthlyTrend DTOs sorted by month ASC
     */
    @Transactional(readOnly = true)
    public List<MonthlyTrend> getMonthlyTrends() {
        log.info("Building monthly trends");

        List<Object[]> results = transactionRepository.monthlyTrends();

        Map<String, MonthlyTrendBuilder> trendMap = new HashMap<>();

        for (Object[] row : results) {
            String month = (String) row[0];

            // ── KEY FIX ─────────────────────────────────────────────
            // row[1] can be either a TransactionType enum OR a String
            // depending on the JPA provider and JPQL implementation.
            // We handle BOTH cases safely here.
            TransactionType type = resolveTransactionType(row[1]);

            BigDecimal amount = (BigDecimal) row[2];

            trendMap.computeIfAbsent(month, MonthlyTrendBuilder::new)
                    .add(type, amount);
        }

        return trendMap.values().stream()
                .map(MonthlyTrendBuilder::build)
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the 10 most recent non-deleted transactions.
     *
     * @return list of TransactionResponse DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions() {
        log.info("Fetching recent {} transactions", RECENT_LIMIT);

        List<Transaction> transactions =
                transactionRepository.findRecentTransactions(
                        PageRequest.of(0, RECENT_LIMIT)
                );

        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Safely resolves a TransactionType from either:
     * - A TransactionType enum (returned by some JPA providers)
     * - A String value like "INCOME" or "EXPENSE"
     *
     * This prevents ClassCastException regardless of
     * how the JPQL query returns the enum column.
     *
     * @param value the raw value from JPQL Object[] row
     * @return resolved TransactionType
     */
    private TransactionType resolveTransactionType(Object value) {
        if (value instanceof TransactionType) {
            return (TransactionType) value;
        }
        if (value instanceof String) {
            return TransactionType.valueOf((String) value);
        }
        throw new IllegalStateException(
                "Unexpected type for TransactionType column: "
                        + value.getClass().getName()
        );
    }

    /**
     * Internal builder — accumulates income and expense
     * totals per month before building the MonthlyTrend DTO.
     */
    private static class MonthlyTrendBuilder {

        private final String     month;
        private       BigDecimal income  = BigDecimal.ZERO;
        private       BigDecimal expense = BigDecimal.ZERO;

        MonthlyTrendBuilder(String month) {
            this.month = month;
        }

        void add(TransactionType type, BigDecimal amount) {
            if (type == TransactionType.INCOME) {
                income = income.add(amount);
            } else {
                expense = expense.add(amount);
            }
        }

        MonthlyTrend build() {
            return MonthlyTrend.builder()
                    .month(month)
                    .income(income)
                    .expense(expense)
                    .build();
        }
    }
}
