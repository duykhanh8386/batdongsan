package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.property.PropertyTrashDto;
import com.BTL.Springboot.entity.PropertyTrashBin;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.mapper.PropertyTrashMapper;
import com.BTL.Springboot.mapper.PropertyMapper;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.PropertyTrashService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PropertyTrashServiceImpl implements PropertyTrashService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PropertyTrashRepository propertyTrashRepository;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private PropertyTrashMapper propertyTrashMapper;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public PropertyTrashDto getDeletedProperty(Integer id) {
        PropertyTrashBin propertyTrashBin = propertyTrashRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bất động sản đã xóa với ID: " + id));
        return propertyTrashMapper.toDto(propertyTrashBin);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public Map<String, Object> findAllDeletedPropertiesWithDetails() {
        List<PropertyTrashBin> deletedProperties = propertyTrashRepository.findAll();
        List<PropertyTrashDto> propertyDtos = new ArrayList<>();
        Map<Integer, String> remainingTimeMap = new HashMap<>();
        Map<Integer, Long> remainingDaysMap = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        for (PropertyTrashBin propertyTrashBin : deletedProperties) {
            Property property = propertyTrashBin.getProperty();
            if (property != null) {
                PropertyTrashDto dto = propertyTrashMapper.toDto(propertyTrashBin);
                propertyDtos.add(dto);

                LocalDateTime expiryDate = propertyTrashBin.getDeletedAt().plusDays(60);
                if (now.isBefore(expiryDate)) {
                    Duration duration = Duration.between(now, expiryDate);
                    long days = duration.toDays();
                    long hours = duration.toHours() % 24;
                    long minutes = duration.toMinutes() % 60;
                    String remainingTime = String.format("%d ngày, %d giờ, %d phút", days, hours, minutes);
                    remainingTimeMap.put(propertyTrashBin.getId(), remainingTime);
                    remainingDaysMap.put(propertyTrashBin.getId(), days);
                } else {
                    remainingTimeMap.put(propertyTrashBin.getId(), "Hết hạn khôi phục");
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("properties", propertyDtos);
        result.put("remainingTimeMap", remainingTimeMap);
        result.put("remainingDaysMap", remainingDaysMap);
        return result;
    }

    @Override
    public void restoreProperty(Integer id) {
        Optional<PropertyTrashBin> propertyTrashBinEntity = propertyTrashRepository.findById(id);
        int propertyId = propertyTrashBinEntity.get().getProperty().getPropertyId();
        Property property = propertyRepository.findById(propertyId);
        property.setStatus("true");
        propertyRepository.save(property);

        propertyImageRepository.restoreImagesByProject(id);
        appointmentRepository.restoreAppointmentsByProject(id);
        transactionRepository.restoreTransactionsByProject(id);
        paymentRepository.restorePaymentsByProject(id);

        propertyTrashRepository.deleteById(id);
    }

    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void cleanExpiredTrash() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60);
        List<PropertyTrashBin> expired = propertyTrashRepository.findByDeletedAtBefore(threshold);

        for (PropertyTrashBin trash : expired) {
            propertyTrashRepository.delete(trash);
        }
    }
}