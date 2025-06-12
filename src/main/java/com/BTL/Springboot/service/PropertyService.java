package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.entity.Property;

import java.util.List;

public interface PropertyService {

    // Hàm lấy tất cả dữ liệu properties
    List<PropertyDto> getAllProperties();

    // Hàm lấy dữ liệu của properties theo propertyId
    PropertyDto getPropertyById(Integer id);

    // Hàm lấy tất cả dữ liệu properties theo listingType và status
    List<PropertyDto> findAllByListingTypeAndStatus(String listingType, String status);

    // Hàm lưu thông tin properties
    PropertyDto saveProperty(PropertyRequest request, Integer propertyId);

    // Hàm tạo mới properties
    PropertyDto createProperty(PropertyRequest request);

    PropertyDto updateProperty(PropertyRequest request);

    // Hàm kiểm tra propertyCode
    boolean existsByPropertyCode(String propertyCode);

    void deleteProperty(Integer id);

    List<Property> getPropertyByProjectId(int id);
}
