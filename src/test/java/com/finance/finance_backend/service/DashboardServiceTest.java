package com.finance.finance_backend.service;

import static org.junit.jupiter.api.Assertions.*;



import com.finance.finance_backend.dto.DashboardResponse;
import com.finance.finance_backend.dto.DashboardResponse.MonthlyTrend;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.Role;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardService.
 * Tests totals, category breakdown, trends, and recent activity.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Super Admin")
                .email("admin@finance.com")
                .role(Role.ADMIN)
                .isActive(true)
                .build();
    }

    // ─────────────────────────────────────────
    // SUMMARY TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Dashboard summary — returns correct totals and net balance")
    void getDashboardSummary_returnsCorrectTotalsAndNetBalance() {
        when(transactionRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("115000.00"));
        when(transactionRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("23000.00"));
        when(transactionRepository.sumGroupedByCategory())
                .thenReturn(List.of());
        when(transactionRepository.monthlyTrends())
                .thenReturn(List.of());
        when(transactionRepository.findRecentTransactions(any(Pageable.class)))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboardSummary();

        assertThat(response).isNotNull();
        assertThat(response.getTotalIncome())
                .isEqualByComparingTo(new BigDecimal("115000.00"));
        assertThat(response.getTotalExpenses())
                .isEqualByComparingTo(new BigDecimal("23000.00"));
        assertThat(response.getNetBalance())
                .isEqualByComparingTo(new BigDecimal("92000.00"));
    }

    @Test
    @DisplayName("Dashboard summary — net balance = income minus expenses")
    void getDashboardSummary_netBalance_isIncomeMinusExpenses() {
        when(transactionRepository.sumByType(TransactionType.INCOME))
                .thenReturn(new BigDecimal("100000.00"));
        when(transactionRepository.sumByType(TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("40000.00"));
        when(transactionRepository.sumGroupedByCategory())
                .thenReturn(List.of());
        when(transactionRepository.monthlyTrends())
                .thenReturn(List.of());
        when(transactionRepository.findRecentTransactions(any()))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboardSummary();

        assertThat(response.getNetBalance())
                .isEqualByComparingTo(new BigDecimal("60000.00"));
    }

    @Test
    @DisplayName("Total by type — returns zero when no transactions")
    void getTotalByType_returnsZeroWhenNoTransactions() {
        when(transactionRepository.sumByType(TransactionType.INCOME))
                .thenReturn(null);

        BigDecimal total =
                dashboardService.getTotalByType(TransactionType.INCOME);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─────────────────────────────────────────
    // CATEGORY BREAKDOWN TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Category breakdown — returns correct map")
    void getCategoryBreakdown_returnsCorrectMap() {
        List<Object[]> mockResults = List.of(
                new Object[]{"Salary", new BigDecimal("115000.00")},
                new Object[]{"Rent",   new BigDecimal("15000.00")},
                new Object[]{"Food",   new BigDecimal("8000.00")}
        );

        when(transactionRepository.sumGroupedByCategory())
                .thenReturn(mockResults);

        Map<String, BigDecimal> breakdown =
                dashboardService.getCategoryBreakdown();

        assertThat(breakdown).hasSize(3);
        assertThat(breakdown.get("Salary"))
                .isEqualByComparingTo(new BigDecimal("115000.00"));
        assertThat(breakdown.get("Rent"))
                .isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(breakdown.get("Food"))
                .isEqualByComparingTo(new BigDecimal("8000.00"));
    }

    @Test
    @DisplayName("Category breakdown — returns empty map when no data")
    void getCategoryBreakdown_returnsEmptyMapWhenNoData() {
        when(transactionRepository.sumGroupedByCategory())
                .thenReturn(List.of());

        Map<String, BigDecimal> breakdown =
                dashboardService.getCategoryBreakdown();

        assertThat(breakdown).isEmpty();
    }

    // ─────────────────────────────────────────
    // MONTHLY TRENDS TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Monthly trends — returns correct income and expense per month")
    void getMonthlyTrends_returnsCorrectTrendsPerMonth() {
        List<Object[]> mockResults = List.of(
                new Object[]{"2024-01", "INCOME",  new BigDecimal("55000.00")},
                new Object[]{"2024-01", "EXPENSE", new BigDecimal("15000.00")},
                new Object[]{"2024-02", "INCOME",  new BigDecimal("60000.00")},
                new Object[]{"2024-02", "EXPENSE", new BigDecimal("8000.00")}
        );

        when(transactionRepository.monthlyTrends())
                .thenReturn(mockResults);

        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();

        assertThat(trends).hasSize(2);

        MonthlyTrend jan = trends.stream()
                .filter(t -> t.getMonth().equals("2024-01"))
                .findFirst()
                .orElseThrow();

        assertThat(jan.getIncome())
                .isEqualByComparingTo(new BigDecimal("55000.00"));
        assertThat(jan.getExpense())
                .isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("Monthly trends — returns empty list when no data")
    void getMonthlyTrends_returnsEmptyListWhenNoData() {
        when(transactionRepository.monthlyTrends())
                .thenReturn(List.of());

        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();

        assertThat(trends).isEmpty();
    }

    // ─────────────────────────────────────────
    // RECENT TRANSACTIONS TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Recent transactions — returns max 10 transactions")
    void getRecentTransactions_returnsMaxTen() {
        Transaction t = Transaction.builder()
                .id(1L)
                .user(mockUser)
                .amount(new BigDecimal("50000.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .isDeleted(false)
                .build();

        when(transactionRepository.findRecentTransactions(any(Pageable.class)))
                .thenReturn(List.of(t));

        List<TransactionResponse> recent =
                dashboardService.getRecentTransactions();

        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Recent transactions — fetches with limit of 10")
    void getRecentTransactions_fetchesWithLimitOfTen() {
        when(transactionRepository.findRecentTransactions(any()))
                .thenReturn(List.of());

        dashboardService.getRecentTransactions();

        verify(transactionRepository).findRecentTransactions(
                argThat(p -> p.getPageSize() == 10)
        );
    }
}