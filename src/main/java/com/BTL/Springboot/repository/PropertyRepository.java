package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    // Thêm điều kiện status = true
    List<Property> findAllByListingTypeAndStatusTrue(String listingType);

    // Trường hợp linh hoạt
    @Query("SELECT p FROM Property p " +
            "LEFT JOIN FETCH p.project pr " +
            "LEFT JOIN FETCH p.listingAgent e " +
            "LEFT JOIN FETCH p.owner c " +
            "WHERE p.listingType = :listingType AND p.status = :status")
    List<Property> findAllByListingTypeAndStatus(@Param("listingType") String listingType,
                                                 @Param("status") String status);

    Optional<Property> findByPropertyCode(String propertyCode);

    // Hàm kiểm tra sự tồn tại của propertyCode
    boolean existsByPropertyCode(String propertyCode);
}
