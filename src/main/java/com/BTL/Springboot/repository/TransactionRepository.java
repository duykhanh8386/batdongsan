package com.BTL.Springboot.repository;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.entity.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Integer> {
    @Modifying
    @Transactional
    @Query(value = """
    UPDATE transactions
    SET status = 'cancelled'
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void cancelTransactionsByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE transactions
    SET status = 'true'
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void restoreTransactionsByProject(@Param("projectId") Integer projectId);

    long countByTransactionDateAndStatus(LocalDate date, String status);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transaction t " +
            "WHERE MONTH(t.transactionDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(t.transactionDate) = YEAR(CURRENT_DATE) " +
            "AND t.status = 'completed'")
    double sumRevenueThisMonth();

    @Query(value = "SELECT COALESCE(SUM(t.price), 0) FROM transactions t " +
            "WHERE t.status = 'completed' " +
            "AND MONTH(t.transaction_date) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
            "AND YEAR(t.transaction_date) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)",
            nativeQuery = true)
    double sumRevenueLastMonth();

    @Query("SELECT t FROM Transaction t JOIN FETCH t.agent a ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(Pageable pageable);

    @Query("SELECT new com.BTL.Springboot.dto.response.transaction.SaleTransactionDto(" +
            "CONCAT(t.buyer.firstName, ' ', t.buyer.lastName), " +
            "t.property.title, t.price, t.status) " +
            "FROM Transaction t " +
            "WHERE t.createdAt BETWEEN :start AND :end " +
            "ORDER BY t.createdAt DESC")
    List<SaleTransactionDto> findSalesBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              Pageable pageable);

    @Query("SELECT COUNT(t), SUM(t.price) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    List<Object[]> getSalesAndRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
