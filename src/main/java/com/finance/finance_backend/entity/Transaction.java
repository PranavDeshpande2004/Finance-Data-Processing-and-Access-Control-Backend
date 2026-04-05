package com.finance.finance_backend.entity;

import com.finance.finance_backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions", indexes = {
        @Index(name = "idx_user_id",    columnList = "user_id"),
        @Index(name = "idx_type",       columnList = "type"),
        @Index(name = "idx_category",   columnList = "category"),
        @Index(name = "idx_date",       columnList = "date"),
        @Index(name = "idx_is_deleted", columnList = "is_deleted")
})

public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false,precision = 15,scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private TransactionType type;

    @Column(nullable = false,length = 100)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Boolean isDeleted=false;

    @Column
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
