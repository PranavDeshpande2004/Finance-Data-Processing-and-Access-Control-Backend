package com.finance.finance_backend.controller;


import com.finance.finance_backend.dto.ApiResponse;
import com.finance.finance_backend.dto.DashboardResponse;
import com.finance.finance_backend.dto.DashboardResponse.MonthlyTrend;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.ratelimit.RateLimitInterceptor;
import com.finance.finance_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for dashboard summary and analytics endpoints.
 * Viewer + Analyst + Admin can access all dashboard endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VIEWER','ANALYST','ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dashboard", description = "Summary and analytics endpoints")
public class DashboardController {

    private final DashboardService     dashboardService;
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Returns the complete dashboard summary including totals,
     * category breakdown, monthly trends, and recent transactions.
     * All roles.
     *
     * @return full DashboardResponse DTO
     */
    @Operation(
            summary     = "Get dashboard summary",
            description = "Returns income, expenses, net balance, trends and recent activity"
    )
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardSummary(
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/dashboard/summary");
        DashboardResponse response = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard summary fetched successfully", response)
        );
    }

    /**
     * Returns total income or total expenses.
     * All roles.
     *
     * @param type INCOME or EXPENSE
     * @return total amount as BigDecimal
     */
    @Operation(
            summary     = "Get total by type",
            description = "Returns total INCOME or total EXPENSE amount"
    )
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalByType(
            @RequestParam TransactionType type,
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/dashboard/total?type={}", type);
        BigDecimal total = dashboardService.getTotalByType(type);
        return ResponseEntity.ok(
                ApiResponse.success("Total fetched successfully", total)
        );
    }

    /**
     * Returns total amount grouped by category.
     * All roles.
     *
     * @return map of category name to total amount
     */
    @Operation(
            summary     = "Get category breakdown",
            description = "Returns total income and expense grouped by category"
    )
    @GetMapping("/category-breakdown")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getCategoryBreakdown(
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/dashboard/category-breakdown");
        Map<String, BigDecimal> breakdown =
                dashboardService.getCategoryBreakdown();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category breakdown fetched successfully", breakdown
                )
        );
    }

    /**
     * Returns monthly income and expense totals.
     * All roles.
     *
     * @return list of MonthlyTrend DTOs
     */
    @Operation(
            summary     = "Get monthly trends",
            description = "Returns income and expense totals grouped by month"
    )
    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyTrend>>> getMonthlyTrends(
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/dashboard/trends/monthly");
        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();
        return ResponseEntity.ok(
                ApiResponse.success("Monthly trends fetched successfully", trends)
        );
    }

    /**
     * Returns the 10 most recent non-deleted transactions.
     * All roles.
     *
     * @return list of TransactionResponse DTOs
     */
    @Operation(
            summary     = "Get recent transactions",
            description = "Returns the 10 most recent financial transactions"
    )
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions(
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/dashboard/recent");
        List<TransactionResponse> recent =
                dashboardService.getRecentTransactions();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Recent transactions fetched successfully", recent
                )
        );
    }
}
