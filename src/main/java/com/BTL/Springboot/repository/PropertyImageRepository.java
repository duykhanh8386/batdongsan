package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Integer> {

    List<PropertyImage> findByProperty(Property property);

    PropertyImage findByImageUrl(String imageUrl);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE property_images
    SET is_main = false
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void softDeleteImagesByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE property_images
    SET is_main = true
    WHERE property_id IN (
        SELECT property_id FROM properties WHERE project_id = :projectId
    )
    """, nativeQuery = true)
    void restoreImagesByProject(@Param("projectId") Integer projectId);
}