package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Appointment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment,Integer> {
    @Modifying
    @Transactional
    @Query(value = """
    UPDATE appointments
    SET status = 'cancelled'
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void cancelAppointmentsByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE appointments
    SET status = 'scheduled'
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void restoreAppointmentsByProject(@Param("projectId") Integer projectId);
}
