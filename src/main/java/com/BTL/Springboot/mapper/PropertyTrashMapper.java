package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.request.property.PropertyTrashRequest;
import com.BTL.Springboot.dto.response.property.PropertyTrashDto;
import com.BTL.Springboot.entity.PropertyTrashBin;
import org.springframework.stereotype.Component;

@Component
public class PropertyTrashMapper {

    public PropertyTrashDto toDto(PropertyTrashBin propertyTrashBin) {
        if (propertyTrashBin == null) {
            return null;
        }

        PropertyTrashDto dto = new PropertyTrashDto();
        dto.setId(propertyTrashBin.getId());
        dto.setProperty(propertyTrashBin.getProperty());
        dto.setDeletedAt(propertyTrashBin.getDeletedAt());

        return dto;
    }

    public PropertyTrashBin toEntity(PropertyTrashDto dto) {
        if (dto == null) {
            return null;
        }

        PropertyTrashBin propertyTrashBin = new PropertyTrashBin();
        propertyTrashBin.setId(dto.getId());
        propertyTrashBin.setProperty(dto.getProperty());
        propertyTrashBin.setDeletedAt(dto.getDeletedAt());

        return propertyTrashBin;
    }

    public PropertyTrashBin toEntity(PropertyTrashRequest request) {
        if (request == null) {
            return null;
        }

        PropertyTrashBin propertyTrashBin = new PropertyTrashBin();
        propertyTrashBin.setProperty(request.getProperty());
        propertyTrashBin.setDeletedAt(request.getDeletedAt());

        return propertyTrashBin;
    }

    public PropertyTrashRequest toDto(PropertyTrashDto dto) {
        if (dto == null) {
            return null;
        }

        PropertyTrashRequest request = new PropertyTrashRequest();
        request.setProperty(dto.getProperty());
        request.setDeletedAt(dto.getDeletedAt());

        return request;
    }
}
