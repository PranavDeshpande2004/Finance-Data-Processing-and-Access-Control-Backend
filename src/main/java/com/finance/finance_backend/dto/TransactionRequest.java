package com.finance.finance_backend.dto;

import com.finance.finance_backend.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

//Request DTO for creating or updating a financial transaction
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TransactionRequest {
    @NotNull(message = "Amount is Required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is Required")
    private TransactionType type;

    @NotBlank(message = "Category is Required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Date is Required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
