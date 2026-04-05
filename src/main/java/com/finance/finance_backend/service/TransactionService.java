package com.finance.finance_backend.service;

import com.finance.finance_backend.dto.TransactionRequest;
import com.finance.finance_backend.dto.PagedResponse;
import com.finance.finance_backend.dto.TransactionResponse;
import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.TransactionType;
import com.finance.finance_backend.exception.ResourceNotFoundException;
import com.finance.finance_backend.repository.TransactionRepository;
import com.finance.finance_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service for managing financial transaction records.
 * Handles CRUD, filtering, search, pagination, and soft delete.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository        userRepository;

    /**
     * Creates a new financial transaction record.
     *
     * @param request the transaction request DTO
     * @param email   the email of the authenticated admin user
     * @return created TransactionResponse DTO
     */
    @Transactional
    public TransactionResponse createTransaction(
            TransactionRequest request,
            String email
    ) {
        log.info("Creating transaction by user: {}", email);

        User user = findUserByEmail(email);

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .isDeleted(false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", saved.getId());

        return TransactionResponse.fromEntity(saved);
    }

    /**
     * Retrieves all non-deleted transactions with optional filters
     * and pagination support.
     *
     * @param type      filter by INCOME or EXPENSE (optional)
     * @param category  filter by category (optional)
     * @param startDate filter from date (optional)
     * @param endDate   filter to date (optional)
     * @param search    keyword search on notes and category (optional)
     * @param page      page number (0-based)
     * @param size      page size
     * @param sortBy    field to sort by
     * @param sortDir   sort direction ASC or DESC
     * @return paginated TransactionResponse list
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getAllTransactions(
            TransactionType type,
            String          category,
            LocalDate       startDate,
            LocalDate       endDate,
            String          search,
            int             page,
            int             size,
            String          sortBy,
            String          sortDir
    ) {
        log.info("Fetching transactions — page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponse> resultPage =
                transactionRepository
                        .findAllWithFilters(
                                type, category, startDate, endDate, search, pageable
                        )
                        .map(TransactionResponse::fromEntity);

        return PagedResponse.fromPage(resultPage);
    }

    /**
     * Retrieves a single transaction by ID.
     *
     * @param id the transaction ID
     * @return TransactionResponse DTO
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        log.info("Fetching transaction with id: {}", id);
        Transaction transaction = findActiveTransactionById(id);
        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * Updates an existing transaction record.
     *
     * @param id      the transaction ID
     * @param request the update request DTO
     * @return updated TransactionResponse DTO
     */
    @Transactional
    public TransactionResponse updateTransaction(
            Long               id,
            TransactionRequest request
    ) {
        log.info("Updating transaction with id: {}", id);
        Transaction transaction = findActiveTransactionById(id);

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDate(request.getDate());
        transaction.setNotes(request.getNotes());

        transactionRepository.save(transaction);
        log.info("Transaction updated successfully with id: {}", id);

        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * Soft deletes a transaction by setting isDeleted = true.
     * The record is never removed from the database.
     *
     * @param id the transaction ID
     */
    @Transactional
    public void deleteTransaction(Long id) {
        log.info("Soft deleting transaction with id: {}", id);
        Transaction transaction = findActiveTransactionById(id);

        transaction.setIsDeleted(true);
        transaction.setDeletedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        log.info("Transaction soft deleted with id: {}", id);
    }

    /**
     * Finds a non-deleted transaction or throws ResourceNotFoundException.
     */
    private Transaction findActiveTransactionById(Long id) {
        return transactionRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction", id)
                );
    }

    /**
     * Finds a user by email or throws ResourceNotFoundException.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }
}
