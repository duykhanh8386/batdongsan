package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.entity.PropertyTrashBin;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.mapper.PropertyTrashMapper;
import com.BTL.Springboot.mapper.PropertyMapper;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTrashRepository propertyTrashRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private PropertyTrashMapper propertyTrashMapper;

    public void validatePropertyRequest(PropertyRequest request, Integer propertyId) {
        // Kiểm tra propertyCode
        String propertyCode = request.getPropertyCode() != null ? request.getPropertyCode().trim() : null;
        if (propertyCode == null || propertyCode.isEmpty()) {
            log.error("Property code is null or empty");
            throw new IllegalArgumentException("Mã bất động sản không được để trống.");
        }

        // Kiểm tra trùng propertyCode
        if (propertyId != null) {
            // Cập nhật bất động sản
            Property existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bất động sản với ID: " + propertyId));
            String existingPropertyCode = existingProperty.getPropertyCode() != null ? existingProperty.getPropertyCode().trim() : "";
            if (!propertyCode.equals(existingPropertyCode) && existsByPropertyCode(propertyCode)) {
                log.error("Property code {} already exists", propertyCode);
                throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
            }
        } else {
            // Tạo mới bất động sản
            if (existsByPropertyCode(propertyCode)) {
                log.error("Property code {} already exists", propertyCode);
                throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
            }
        }

        // Kiểm tra listingType
        if (request.getListingType() == null || (!request.getListingType().equals("Bán") && !request.getListingType().equals("Cho thuê"))) {
            log.error("Invalid listing type: {}", request.getListingType());
            throw new IllegalArgumentException("Loại giao dịch phải là 'Bán' hoặc 'Cho thuê'.");
        }

        // Kiểm tra propertyType
        if (request.getPropertyType() == null || request.getPropertyType().getTypeId() == null) {
            log.error("Property type is null or invalid");
            throw new IllegalArgumentException("Loại bất động sản không được để trống.");
        }

        // Kiểm tra listingAgent
        if (request.getListingAgent() == null || request.getListingAgent().getEmployeeId() == null) {
            log.error("Listing agent is null or invalid");
            throw new IllegalArgumentException("Nhân viên phụ trách không được để trống.");
        }

        // Kiểm tra owner
        if (request.getOwner() == null || request.getOwner().getCustomerId() == null) {
            log.error("Owner is null or invalid");
            throw new IllegalArgumentException("Chủ sở hữu không được để trống.");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public PropertyDto saveProperty(PropertyRequest request, Integer propertyId) {
        // Kiểm tra điều kiện
        validatePropertyRequest(request, propertyId);

        Property property = propertyMapper.toEntity(request);
        if (propertyId != null) {
            // Cập nhật bất động sản
            Property existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bất động sản với ID: " + propertyId));
            property.setPropertyId(propertyId);
            property.setCreatedAt(existingProperty.getCreatedAt()); // Giữ nguyên createdAt
            property.setUpdatedAt(LocalDateTime.now()); // Cập nhật updatedAt
        } else {
            // Tạo mới bất động sản
            property.setCreatedAt(LocalDateTime.now()); // Đặt createdAt khi tạo mới
            property.setUpdatedAt(LocalDateTime.now()); // Cập nhật updatedAt
        }

        Property savedProperty = propertyRepository.save(property);
        log.info("Saved property with ID: {}", savedProperty.getPropertyId());
        return propertyMapper.toDto(savedProperty);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public PropertyDto createProperty(PropertyRequest request) {
        // Kiểm tra điều kiện
        validatePropertyRequest(request, null);

        Property property = propertyMapper.toEntity(request);
        if (property.getStatus() == null) {
            property.setStatus("true");
        }
        if (property.getIsFurnished() == null) {
            property.setIsFurnished(false);
        }
        property.setCreatedAt(LocalDateTime.now());
        property.setUpdatedAt(LocalDateTime.now());
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toDto(savedProperty);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public PropertyDto updateProperty(PropertyRequest request) {
        String propertyCode = request.getPropertyCode() != null ? request.getPropertyCode().trim() : null;
        Property existingProperty = propertyRepository.findByPropertyCode(propertyCode)
                .orElseThrow(() -> new IllegalArgumentException("Bất động sản với mã " + propertyCode + " không tồn tại."));
        validatePropertyRequest(request, existingProperty.getPropertyId());
        return saveProperty(request, existingProperty.getPropertyId());
    }

    // Các phương thức khác giữ nguyên
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public List<PropertyDto> getAllProperties() {
        List<Property> properties = propertyRepository.findAll();
        return properties.stream().map(propertyMapper::toDto).collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public PropertyDto getPropertyById(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        return propertyMapper.toDto(property);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public List<PropertyDto> findAllByListingTypeAndStatus(String listingType, String status) {
        List<Property> properties = propertyRepository.findAllByListingTypeAndStatus(listingType, status);
        return properties.stream().map(propertyMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public boolean existsByPropertyCode(String propertyCode) {
        boolean exists = propertyRepository.existsByPropertyCode(propertyCode);
        log.info("Checked propertyCode {}: exists={}", propertyCode, exists);
        return exists;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public void deleteProperty(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bất động sản với ID: " + id));

        if ("false".equals(property.getStatus())) {
            log.warn("Bất động sản với ID {} đã bị xóa trước đó", id);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        property.setStatus("false");
        property.setUpdatedAt(now);

        propertyImageRepository.softDeleteImagesByProject(id);
        appointmentRepository.cancelAppointmentsByProject(id);
        transactionRepository.cancelTransactionsByProject(id);
        paymentRepository.cancelPaymentsByProject(id);

        PropertyTrashBin propertyTrashBin = new PropertyTrashBin();
        propertyTrashBin.setProperty(property);
        propertyTrashBin.setDeletedAt(now);

        propertyRepository.save(property);
        propertyTrashRepository.save(propertyTrashBin);
        log.info("Đã xóa mềm bất động sản với ID: {} và lưu vào bảng deleted_properties", id);
    }

    @Override
    public List<Property> getPropertyByProjectId(int id) {
        return propertyRepository.findPropertiesByProjectId(id);
    }
}