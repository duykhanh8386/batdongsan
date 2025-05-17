package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    // Thêm điều kiện status = true
    List<Property> findAllByListingTypeAndStatusTrue(String listingType);

    // Trường hợp linh hoạt
    List<Property> findAllByListingTypeAndStatus(String listingType, String status);

    Optional<Property> findByPropertyCode(String propertyCode);

    // Hàm kiểm tra sự tồn tại của propertyCode
    boolean existsByPropertyCode(String propertyCode);
}
