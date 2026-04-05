//package com.finance.finance_backend.repository;
//
//import com.finance.finance_backend.entity.Transaction;
//import com.finance.finance_backend.enums.TransactionType;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
////Repository for Transaction entity database operations
//
//
//@Repository
//public interface TransactionRepository  extends JpaRepository<Transaction,Long> {
//
//    Optional<Transaction> findByIdAndIsDeletedFalse(Long id);
//
//    //Paginated + filtered query combining all filter params
//    @Query("""
//        SELECT t FROM Transaction t
//        WHERE t.isDeleted = false
//          AND (:type     IS NULL OR t.type     = :type)
//          AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
//          AND (:startDate IS NULL OR t.date   >= :startDate)
//          AND (:endDate   IS NULL OR t.date   <= :endDate)
//          AND (:search    IS NULL OR LOWER(t.notes) LIKE LOWER(CONCAT('%', :search, '%'))
//                                 OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))
//        """)
//    Page<Transaction> findAllWithFilters(
//            @Param("type") TransactionType type,
//            @Param("category") String category,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate,
//            @Param("search") String search,
//            Pageable pageable
//    );
//
//    //Dashboard — total by type (INCOME or EXPENSE)
//    @Query("""
//        SELECT COALESCE(SUM(t.amount), 0)
//        FROM Transaction t
//        WHERE t.isDeleted = false
//          AND t.type = :type
//        """)
//    BigDecimal sumByType(@Param("type")TransactionType type);
//
//
//    //Dashboard — total per category.
//    @Query("""
//        SELECT t.category, COALESCE(SUM(t.amount), 0)
//        FROM Transaction t
//        WHERE t.isDeleted = false
//        GROUP BY t.category
//        """)
//    List<Object[]>sumGroupedByCategory();
//
//    //Dashboard — monthly income and expense trend.
//    @Query("""
//        SELECT FUNCTION('DATE_FORMAT', t.date, '%Y-%m') AS month,
//               t.type,
//               COALESCE(SUM(t.amount), 0)
//        FROM Transaction t
//        WHERE t.isDeleted = false
//        GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m'), t.type
//        ORDER BY month ASC
//        """)
//    List<Object[]>monthlyTrends();
//
//    //Dashboard — 10 most recent transactions
//    @Query("""
//        SELECT t FROM Transaction t
//        WHERE t.isDeleted = false
//        ORDER BY t.createdAt DESC
//        """)
//    List<Transaction>findRecentTransactions(Pageable pageable);
//
//
//}


package com.finance.finance_backend.repository;

import com.finance.finance_backend.entity.Transaction;
import com.finance.finance_backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity.
 * All queries exclude soft-deleted records automatically.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndIsDeletedFalse(Long id);

    /**
     * Paginated + filtered query.
     * Null params are ignored — acts as optional filters.
     */
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.isDeleted = false
          AND (:type     IS NULL OR t.type     = :type)
          AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
          AND (:startDate IS NULL OR t.date   >= :startDate)
          AND (:endDate   IS NULL OR t.date   <= :endDate)
          AND (:search    IS NULL OR LOWER(t.notes) LIKE LOWER(CONCAT('%',:search,'%'))
                                 OR LOWER(t.category) LIKE LOWER(CONCAT('%',:search,'%')))
        """)
    Page<Transaction> findAllWithFilters(
            @Param("type")      TransactionType type,
            @Param("category")  String          category,
            @Param("startDate") LocalDate       startDate,
            @Param("endDate")   LocalDate       endDate,
            @Param("search")    String          search,
            Pageable            pageable
    );

    /**
     * Dashboard — total amount by transaction type.
     */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.isDeleted = false
          AND t.type = :type
        """)
    BigDecimal sumByType(@Param("type") TransactionType type);

    /**
     * Dashboard — total per category.
     */
    @Query("""
        SELECT t.category, COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.isDeleted = false
        GROUP BY t.category
        """)
    List<Object[]> sumGroupedByCategory();

    /**
     * Dashboard — monthly trends.
     * Returns type as STRING using name() to avoid ClassCastException.
     * ← KEY FIX: CAST t.type using string representation
     */
    @Query("""
        SELECT
            FUNCTION('DATE_FORMAT', t.date, '%Y-%m'),
            CASE t.type
                WHEN com.finance.finance_backend.enums.TransactionType.INCOME  THEN 'INCOME'
                WHEN com.finance.finance_backend.enums.TransactionType.EXPENSE THEN 'EXPENSE'
            END,
            COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.isDeleted = false
        GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m'), t.type
        ORDER BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m') ASC
        """)
    List<Object[]> monthlyTrends();

    /**
     * Dashboard — 10 most recent transactions.
     */
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.isDeleted = false
        ORDER BY t.createdAt DESC
        """)
    List<Transaction> findRecentTransactions(Pageable pageable);
}
