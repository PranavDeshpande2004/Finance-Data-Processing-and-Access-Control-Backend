package com.finance.finance_backend.controller;

import com.finance.finance_backend.dto.*;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.ratelimit.RateLimitInterceptor;
import com.finance.finance_backend.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerUnitTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private RateLimitInterceptor rateLimitInterceptor;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequest request;
    private TransactionResponse response;
    private UserDetails user;

    @BeforeEach
    void setUp() {

        request = new TransactionRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory("Salary");
        request.setDate(LocalDate.of(2024, 1, 15));
        request.setNotes("January salary");

        response = TransactionResponse.builder()
                .id(1L)
                .userId(1L)
                .userName("Super Admin")
                .amount(new BigDecimal("50000.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2024, 1, 15))
                .notes("January salary")
                .build();

        user = new User("admin@finance.com", "pass", List.of());

        doNothing().when(rateLimitInterceptor).checkRateLimit(any());
    }

    // ✅ CREATE
    @Test
    void createTransaction_shouldReturnCreated() {

        when(transactionService.createTransaction(any(), any()))
                .thenReturn(response);

        var result = transactionController.createTransaction(request, user, httpRequest);

        assertEquals(201, result.getStatusCodeValue());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Salary", result.getBody().getData().getCategory());

        verify(transactionService).createTransaction(any(), eq(user.getUsername()));
    }

    // ❗ NOTE: Role tests REMOVED (unit test can't check security)

    // ✅ GET ALL
    @Test
    void getAllTransactions_shouldReturnData() {

        PagedResponse<TransactionResponse> paged =
                PagedResponse.<TransactionResponse>builder()
                        .content(List.of(response))
                        .pageNumber(0)
                        .pageSize(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();

        when(transactionService.getAllTransactions(
                any(), any(), any(), any(), any(),
                anyInt(), anyInt(), anyString(), anyString()
        )).thenReturn(paged);

        var result = transactionController.getAllTransactions(
                null, null, null, null, null,
                0, 10, "date", "desc", httpRequest
        );

        assertTrue(result.getBody().isSuccess());
        assertEquals(1, result.getBody().getData().getTotalElements());
    }

    // ✅ GET BY ID
    @Test
    void getTransactionById_shouldReturnData() {

        when(transactionService.getTransactionById(1L))
                .thenReturn(response);

        var result = transactionController.getTransactionById(1L, httpRequest);

        assertEquals(1L, result.getBody().getData().getId());
    }

    // ✅ UPDATE
    @Test
    void updateTransaction_shouldReturnUpdated() {

        when(transactionService.updateTransaction(eq(1L), any()))
                .thenReturn(response);

        var result = transactionController.updateTransaction(1L, request, httpRequest);

        assertTrue(result.getBody().isSuccess());
        assertEquals("Salary", result.getBody().getData().getCategory());
    }

    // ✅ DELETE
    @Test
    void deleteTransaction_shouldReturnSuccess() {

        doNothing().when(transactionService).deleteTransaction(1L);

        var result = transactionController.deleteTransaction(1L, httpRequest);

        assertTrue(result.getBody().isSuccess());
        assertEquals("Transaction deleted successfully", result.getBody().getMessage());

        verify(transactionService).deleteTransaction(1L);
    }
}