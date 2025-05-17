package com.BTL.Springboot.service.impl;

import com.BTL.Springboot.dto.PropertyDto;
import com.BTL.Springboot.dto.request.PropertyRequest;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.mapper.PropertyMapper;
import com.BTL.Springboot.repository.PropertyRepository;
import com.BTL.Springboot.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PropertyServiceImpl implements PropertyService {
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private PropertyMapper mapper;

    /**
     * Get all properties
     */
    @Override
    public List<PropertyDto> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Get property by ID
     */
    @Override
    public PropertyDto getPropertyById(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return mapper.toDto(property);
    }

    @Override
    public PropertyDto getPropertyByCode(String propertyCode) {
        Property property = propertyRepository.findByPropertyCode(propertyCode)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return mapper.toDto(property);
    }

    /**
     * Find properties by listing type and status = true
     */
    @Override
    public List<PropertyDto> findAllByListingTypeAndStatusTrue(String listingType) {
        List<Property> properties = propertyRepository.findAllByListingTypeAndStatusTrue(listingType);
        return properties.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Find properties by listing type and status
     */
    @Override
    public List<PropertyDto> findAllByListingTypeAndStatus(String listingType, String status) {
        List<Property> properties = propertyRepository.findAllByListingTypeAndStatus(listingType, status);
        return properties.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Save a property (create or update)
     */
    @Override
    public PropertyDto saveProperty(PropertyRequest request, Integer propertyId) {
        Property property = mapper.toEntity(request);
        // Kiểm tra propertyCode
        String propertyCode = property.getPropertyCode() != null ? property.getPropertyCode().trim() : null;
        if (propertyCode == null || propertyCode.isEmpty()) {
            log.error("Property code is null or empty");
            throw new IllegalArgumentException("Mã bất động sản không được để trống.");
        }

        if (propertyId != null) {
            // Cập nhật bất động sản
            Optional<Property> existingPropertyOpt = propertyRepository.findById(propertyId);
            if (!existingPropertyOpt.isPresent()) {
                log.error("Property not found with ID: {}", propertyId);
                throw new IllegalArgumentException("Không tìm thấy bất động sản với ID: " + propertyId);
            }
            Property existingProperty = existingPropertyOpt.get();
            String existingPropertyCode = existingProperty.getPropertyCode() != null ? existingProperty.getPropertyCode().trim() : "";
            // Kiểm tra trùng chỉ khi propertyCode thay đổi
            if (!propertyCode.equals(existingPropertyCode) && existsByPropertyCode(propertyCode)) {
                log.error("Property code {} already exists", propertyCode);
                throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
            }
            // Gán propertyId cho entity khi cập nhật
            property.setPropertyId(propertyId);
        } else {
            // Tạo mới bất động sản
            if (existsByPropertyCode(propertyCode)) {
                log.error("Property code {} already exists", propertyCode);
                throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
            }
        }

        // Lưu bất động sản
        Property savedProperty = propertyRepository.save(property);
        log.info("Saved property with ID: {}", savedProperty.getPropertyId());
        return mapper.toDto(savedProperty);
    }

    /**
     * Update property status (soft delete)
     */
    @Override
    public void deletePropertyStatus(Integer id, String status) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        property.setStatus(status);
        propertyRepository.save(property);
    }

    /**
     * Create a new property
     */
    @Override
    public PropertyDto createProperty(PropertyRequest request) {
        Property property = mapper.toEntity(request);
        if (property.getStatus() == null) {
            property.setStatus("true");
        }
        if (property.getIsFurnished() == null) {
            property.setIsFurnished(false);
        }
        property.setCreatedAt(java.time.LocalDateTime.now());
        property.setUpdatedAt(java.time.LocalDateTime.now());
        Property savedProperty = propertyRepository.save(property);
        return mapper.toDto(savedProperty);
    }

    @Override
    public boolean existsByPropertyCode(String propertyCode) {
        boolean exists = propertyRepository.existsByPropertyCode(propertyCode);
        log.info("Checked propertyCode {}: exists={}", propertyCode, exists);
        return exists;
    }
}