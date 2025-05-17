package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.PropertyImageDto;
import com.BTL.Springboot.entity.PropertyImage;

public class PropertyImageMapper {
    // Chuyển entity sang DTO
    public PropertyImageDto toDto(PropertyImage image) {
        PropertyImageDto dto = new PropertyImageDto();
        dto.setImageId(image.getImageId());
        dto.setProperty(image.getProperty());
        dto.setImageUrl(image.getImageUrl());
        dto.setCaption(image.getCaption());
        dto.setIsMain(image.getIsMain());
        dto.setCreatedAt(image.getCreatedAt());
        return dto;
    }
}
