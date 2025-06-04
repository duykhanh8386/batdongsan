package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    List<Property> findByPropertyId(Integer propertyId);

    @Query("SELECT p FROM Property p WHERE p.project.projectId = :projectId")
    List<Property> findPropertiesByProjectId(@Param("projectId") Integer projectId);


    @Modifying
    @Transactional
    @Query("UPDATE Property p SET p.status = 'false' WHERE p.project.projectId = :projectId")
    void softDeletePropertiesByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query("UPDATE Property p SET p.status = 'true' WHERE p.project.projectId = :projectId")
    void restorePropertiesByProject(@Param("projectId") Integer projectId);

}
