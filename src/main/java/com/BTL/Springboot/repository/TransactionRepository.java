package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
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
}
