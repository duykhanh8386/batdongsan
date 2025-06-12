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
    SET status = 'COMPLETED'
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void restoreTransactionsByProject(@Param("projectId") Integer projectId);

    long countByTransactionDateAndStatus(LocalDate date, String status);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transaction t " +
            "WHERE MONTH(t.transactionDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(t.transactionDate) = YEAR(CURRENT_DATE) " +
            "AND t.status = 'COMPLETED'")
    double sumRevenueThisMonth();

    @Query(value = "SELECT COALESCE(SUM(t.price), 0) FROM transactions t " +
            "WHERE t.status = 'COMPLETED' " +
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

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end AND t.status = 'COMPLETED'")
    long countSoldBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end AND t.status = 'COMPLETED'")
    double sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end AND t.status = 'COMPLETED'")
    long countSoldByTransactionDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.price), 0) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end AND t.status = 'COMPLETED'")
    double sumRevenueByTransactionDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Lấy tổng giá theo ngày cho giao dịch "Bán" hoặc "Cho thuê" với trạng thái "COMPLETED"
    @Query("SELECT t.transactionDate, SUM(t.price) FROM Transaction t " +
            "WHERE t.transactionType = :type AND t.status = 'COMPLETED' GROUP BY t.transactionDate ORDER BY t.transactionDate")
    List<Object[]> findTotalPriceByDateAndType(String type);

    // Đếm số lượng giao dịch theo trạng thái cho "Bán" hoặc "Cho thuê"
    @Query("SELECT t.status, COUNT(t) FROM Transaction t WHERE t.transactionType = :type GROUP BY t.status")
    List<Object[]> countTransactionsByStatusAndType(String type);

    @Query("SELECT DISTINCT t.status FROM Transaction t WHERE t.status IN ('TRANSFERRED', 'SIGNED', 'COMPLETED', 'IN_PROGRESS', 'PENDING', 'DEPOSITED')")
    List<String> findAllStatuses();

    // Đếm số lượng giao dịch theo status và transaction_type
    @Query("SELECT t.status, t.transactionType, COUNT(t) FROM Transaction t " +
            "WHERE (:startDate IS NULL OR t.transactionDate >= :startDate) " +
            "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
            "GROUP BY t.status, t.transactionType")
    List<Object[]> countTransactionsByStatusAndType(LocalDate startDate, LocalDate endDate);
}
