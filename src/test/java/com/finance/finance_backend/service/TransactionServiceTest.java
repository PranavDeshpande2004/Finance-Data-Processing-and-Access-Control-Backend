package com.finance.finance_backend.service;

import static org.junit.jupiter.api.Assertions.*;


import com.finance.finance_backend.dto.TransactionRequest;
import com.finance.finance_backend.dto.PagedResponse;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.Role;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.exception.ResourceNotFoundException;
import com.finance.finance_backend.repository.TransactionRepository;
import com.finance.finance_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 * Tests CRUD, soft delete, filters, and pagination.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository        userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User               mockUser;
    private Transaction        mockTransaction;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Super Admin")
                .email("admin@finance.com")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        mockTransaction = Transaction.builder()
                .id(1L)
                .user(mockUser)
                .amount(new BigDecimal("50000.00"))
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2024, 1, 15))
                .notes("January salary")
                .isDeleted(false)
                .build();

        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("50000.00"));
        transactionRequest.setType(TransactionType.INCOME);
        transactionRequest.setCategory("Salary");
        transactionRequest.setDate(LocalDate.of(2024, 1, 15));
        transactionRequest.setNotes("January salary");
    }

    // ─────────────────────────────────────────
    // CREATE TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Create — success — returns TransactionResponse")
    void createTransaction_success_returnsResponse() {
        when(userRepository.findByEmail("admin@finance.com"))
                .thenReturn(Optional.of(mockUser));
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(mockTransaction);

        TransactionResponse response =
                transactionService.createTransaction(
                        transactionRequest, "admin@finance.com"
                );

        assertThat(response).isNotNull();
        assertThat(response.getAmount())
                .isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Create — new transaction isDeleted is false by default")
    void createTransaction_isDeletedFalseByDefault() {
        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(mockUser));
        when(transactionRepository.save(any()))
                .thenReturn(mockTransaction);

        transactionService.createTransaction(
                transactionRequest, "admin@finance.com"
        );

        verify(transactionRepository).save(argThat(t ->
                Boolean.FALSE.equals(t.getIsDeleted())
        ));
    }

    @Test
    @DisplayName("Create — user not found — throws ResourceNotFoundException")
    void createTransaction_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@finance.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.createTransaction(
                        transactionRequest, "unknown@finance.com"
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────
    // READ TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Get by ID — success — returns TransactionResponse")
    void getById_success_returnsResponse() {
        when(transactionRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(mockTransaction));

        TransactionResponse response =
                transactionService.getTransactionById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategory()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Get by ID — not found — throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        when(transactionRepository.findByIdAndIsDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.getTransactionById(999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Get all — returns paginated response")
    void getAllTransactions_returnsPaginatedResponse() {
        Page<Transaction> mockPage = new PageImpl<>(
                List.of(mockTransaction),
                PageRequest.of(0, 10, Sort.by("date").descending()),
                1
        );

        when(transactionRepository.findAllWithFilters(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(mockPage);

        PagedResponse<TransactionResponse> response =
                transactionService.getAllTransactions(
                        null, null, null, null, null,
                        0, 10, "date", "desc"
                );

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.isLast()).isTrue();
    }

    // ─────────────────────────────────────────
    // UPDATE TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Update — success — returns updated TransactionResponse")
    void updateTransaction_success_returnsUpdated() {
        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("55000.00"));
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setCategory("Salary");
        updateRequest.setDate(LocalDate.of(2024, 1, 15));
        updateRequest.setNotes("Revised salary");

        when(transactionRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any()))
                .thenReturn(mockTransaction);

        TransactionResponse response =
                transactionService.updateTransaction(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(transactionRepository).save(argThat(t ->
                t.getAmount().compareTo(new BigDecimal("55000.00")) == 0 &&
                        t.getNotes().equals("Revised salary")
        ));
    }

    @Test
    @DisplayName("Update — not found — throws ResourceNotFoundException")
    void updateTransaction_notFound_throwsException() {
        when(transactionRepository.findByIdAndIsDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.updateTransaction(999L, transactionRequest)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────
    // SOFT DELETE TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Delete — soft delete — sets isDeleted true and deletedAt")
    void deleteTransaction_softDelete_setsIsDeletedTrueAndTimestamp() {
        when(transactionRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any()))
                .thenReturn(mockTransaction);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository).save(argThat(t ->
                Boolean.TRUE.equals(t.getIsDeleted()) &&
                        t.getDeletedAt() != null
        ));
    }

    @Test
    @DisplayName("Delete — record never hard deleted from DB")
    void deleteTransaction_recordNeverHardDeleted() {
        when(transactionRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(mockTransaction));
        when(transactionRepository.save(any())).thenReturn(mockTransaction);

        transactionService.deleteTransaction(1L);

        // deleteById should NEVER be called — soft delete only
        verify(transactionRepository, never()).deleteById(any());
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete — not found — throws ResourceNotFoundException")
    void deleteTransaction_notFound_throwsException() {
        when(transactionRepository.findByIdAndIsDeletedFalse(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.deleteTransaction(999L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}