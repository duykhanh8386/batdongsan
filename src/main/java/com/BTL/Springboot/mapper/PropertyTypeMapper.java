package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.response.property_type.PropertyTypeDto;
import com.BTL.Springboot.entity.PropertyType;
import org.springframework.stereotype.Component;

@Component
public class PropertyTypeMapper {

    public PropertyTypeDto toDto(PropertyType propertyType) {
        if (propertyType == null) {
            return null;
        }

        PropertyTypeDto dto = new PropertyTypeDto();
        dto.setTypeId(propertyType.getTypeId());
        dto.setTypeName(propertyType.getTypeName());
        return dto;
    }

    public PropertyType toEntity(PropertyTypeDto dto) {
        if (dto == null) {
            return null;
        }

        PropertyType propertyType = new PropertyType();
        dto.setTypeId(dto.getTypeId());
        dto.setTypeName(dto.getTypeName());
        return propertyType;
    }
}
