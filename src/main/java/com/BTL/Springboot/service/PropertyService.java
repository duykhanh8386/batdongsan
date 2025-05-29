package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.dto.request.property.PropertyRequest;

import java.util.List;

public interface PropertyService {

    // Hàm lấy tất cả dữ liệu properties
    List<PropertyDto> getAllProperties();

    // Hàm lấy dữ liệu của properties theo propertyId
    PropertyDto getPropertyById(Integer id);

    // Hàm lấy dữ liệu của properties theo propertyCode
    PropertyDto getPropertyByCode(String propertyCode);

    // Hàm lấy tất cả dữ liệu properties theo listingType và statusTrue
    List<PropertyDto> findAllByListingTypeAndStatusTrue(String listingType);

    // Hàm lấy tất cả dữ liệu properties theo listingType và status
    List<PropertyDto> findAllByListingTypeAndStatus(String listingType, String status);

    // Hàm lưu thông tin properties
    PropertyDto saveProperty(PropertyRequest request, Integer propertyId);

    // Hàm xóa properties
    void deletePropertyStatus(Integer id, String status);

    // Hàm tạo mới properties
    PropertyDto createProperty(PropertyRequest request);

    // Hàm kiểm tra propertyCode
    boolean existsByPropertyCode(String propertyCode);
}
