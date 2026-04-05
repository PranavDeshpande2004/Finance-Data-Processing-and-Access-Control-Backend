package com.finance.finance_backend.controller;


import com.finance.finance_backend.dto.TransactionRequest;
import com.finance.finance_backend.dto.ApiResponse;
import com.finance.finance_backend.dto.PagedResponse;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.ratelimit.RateLimitInterceptor;
import com.finance.finance_backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for financial transaction CRUD operations.
 * GET endpoints — ANALYST + ADMIN
 * POST / PUT / DELETE — ADMIN only (enforced in SecurityConfig)
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Transactions", description = "Financial record management endpoints")
public class TransactionController {

    private final TransactionService   transactionService;
    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Creates a new financial transaction.
     * Admin only.
     *
     * @param request     the transaction request body
     * @param userDetails the authenticated user (injected by Spring Security)
     * @return created transaction
     */
    @Operation(
            summary     = "Create transaction",
            description = "Creates a new income or expense record. Admin only."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        rateLimitInterceptor.checkRateLimit(httpRequest);
        log.info("POST /api/transactions — user: {}", userDetails.getUsername());

        TransactionResponse response = transactionService.createTransaction(
                request,
                userDetails.getUsername()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", response));
    }

    /**
     * Retrieves all non-deleted transactions with optional filters,
     * search, and pagination.
     * Analyst + Admin.
     *
     * @param type      filter by INCOME or EXPENSE
     * @param category  filter by category name
     * @param startDate filter from this date
     * @param endDate   filter to this date
     * @param search    keyword search in notes and category
     * @param page      page number (default 0)
     * @param size      page size (default 10)
     * @param sortBy    sort field (default date)
     * @param sortDir   sort direction ASC or DESC (default DESC)
     * @return paginated transaction list
     */
    @Operation(
            summary     = "Get all transactions",
            description = "Returns paginated transactions with optional filters and search"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getAllTransactions(
            @Parameter(description = "Filter by type: INCOME or EXPENSE")
            @RequestParam(required = false) TransactionType type,

            @Parameter(description = "Filter by category name")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter from date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Filter to date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Keyword search in notes and category")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,

            HttpServletRequest httpRequest
    ) {
        rateLimitInterceptor.checkRateLimit(httpRequest);
        log.info("GET /api/transactions — page: {}, size: {}", page, size);

        PagedResponse<TransactionResponse> response =
                transactionService.getAllTransactions(
                        type, category, startDate, endDate,
                        search, page, size, sortBy, sortDir
                );

        return ResponseEntity.ok(
                ApiResponse.success("Transactions fetched successfully", response)
        );
    }

    /**
     * Retrieves a single transaction by ID.
     * Analyst + Admin.
     *
     * @param id the transaction ID
     * @return transaction details
     */
    @Operation(
            summary     = "Get transaction by ID",
            description = "Returns a single transaction by its ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/transactions/{}", id);
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction fetched successfully", response)
        );
    }

    /**
     * Updates an existing transaction by ID.
     * Admin only.
     *
     * @param id      the transaction ID
     * @param request the update request body
     * @return updated transaction details
     */
    @Operation(
            summary     = "Update transaction",
            description = "Updates an existing transaction record. Admin only."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitInterceptor.checkRateLimit(httpRequest);
        log.info("PUT /api/transactions/{}", id);
        TransactionResponse response =
                transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction updated successfully", response)
        );
    }

    /**
     * Soft deletes a transaction by ID.
     * Sets isDeleted = true — record is never removed from DB.
     * Admin only.
     *
     * @param id the transaction ID
     * @return success message
     */
    @Operation(
            summary     = "Delete transaction",
            description = "Soft deletes a transaction (marks as deleted, not removed). Admin only."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("DELETE /api/transactions/{}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction deleted successfully", null)
        );
    }
}


