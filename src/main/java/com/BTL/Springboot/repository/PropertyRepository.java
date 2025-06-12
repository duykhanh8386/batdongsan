package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Integer> {

    Property findById(int id);

    // Thêm điều kiện status = true
    List<Property> findAllByListingTypeAndStatusTrue(String listingType);

    // Trường hợp linh hoạt
    @Query("SELECT p FROM Property p " +
            "LEFT JOIN FETCH p.project pr " +
            "LEFT JOIN FETCH p.listingAgent e " +
            "LEFT JOIN FETCH p.owner c " +
            "WHERE p.listingType = :listingType AND p.status = :status ORDER BY p.createdAt DESC")
    List<Property> findAllByListingTypeAndStatus(@Param("listingType") String listingType,
                                                 @Param("status") String status);

    Optional<Property> findByPropertyCode(String propertyCode);

    // Hàm kiểm tra sự tồn tại của propertyCode
    boolean existsByPropertyCode(String propertyCode);

    List<Property> findByPropertyId(Integer propertyId);

    @Query("SELECT p FROM Property p WHERE p.project.projectId = :projectId")
    List<Property> findPropertiesByProjectId(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query("UPDATE Property p SET p.status = 'true' WHERE p.project.projectId = :projectId")
    void softDeletePropertiesByProject(@Param("projectId") Integer projectId);

    @Modifying
    @Transactional
    @Query("UPDATE Property p SET p.status = 'false' WHERE p.project.projectId = :projectId")
    void restorePropertiesByProject(@Param("projectId") Integer projectId);
}
