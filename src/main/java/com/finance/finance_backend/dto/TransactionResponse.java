package com.finance.finance_backend.dto;

import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction transaction){
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getName())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
