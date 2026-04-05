package com.zorvyn.finance.repository;

import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.TransactionType;
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

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {


    Page<FinancialRecord> findByDeletedFalse(Pageable pageable);

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);


    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deleted = false
          AND (:type IS NULL OR r.type = :type)
          AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
          AND (:startDate IS NULL OR r.date >= :startDate)
          AND (:endDate IS NULL OR r.date <= :endDate)
    """)
    Page<FinancialRecord> findWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("""
        SELECT r.category, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false
        GROUP BY r.category
        ORDER BY SUM(r.amount) DESC
    """)
    List<Object[]> sumByCategory();

    @Query("""
        SELECT FUNCTION('TO_CHAR', r.date, 'YYYY-MM') AS month, r.type, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false
          AND r.date >= :startDate
        GROUP BY FUNCTION('TO_CHAR', r.date, 'YYYY-MM'), r.type
        ORDER BY month ASC
    """)
    List<Object[]> monthlyTrends(@Param("startDate") LocalDate startDate);


    List<FinancialRecord> findTop10ByDeletedFalseOrderByCreatedAtDesc();
}
