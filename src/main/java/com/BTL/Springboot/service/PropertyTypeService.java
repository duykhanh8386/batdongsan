package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.property_type.PropertyTypeDto;
import com.BTL.Springboot.entity.PropertyType;

import java.util.List;

public interface PropertyTypeService {
    List<PropertyType> getAllPropertyType();

    List<PropertyTypeDto> getAllPropertyTypes();
    PropertyType getPropertyTypeById(Integer typeId);
    public PropertyType getPropertyTypeByName(String typeName);

}
