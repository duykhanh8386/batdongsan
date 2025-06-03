package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Payment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    @Modifying
    @Transactional
    @Query(value = """
    UPDATE payments
    SET status = 'cancelled'
    WHERE transaction_id IN (
        SELECT transaction_id FROM transactions
        WHERE property_id IN (
            SELECT property_id FROM properties WHERE project_id = :projectId
        )
    )
    """, nativeQuery = true)
    void cancelPaymentsByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE payments
    SET status = 'true'
    WHERE transaction_id IN (
        SELECT transaction_id FROM transactions
        WHERE property_id IN (
            SELECT property_id FROM properties WHERE project_id = :projectId
        )
    )
    """, nativeQuery = true)
    void restorePaymentsByProject(@Param("projectId") Integer projectId);

}
