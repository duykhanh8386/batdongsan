package com.BTL.Springboot.service;

import com.BTL.Springboot.entity.PropertyType;

import java.util.List;
import java.util.Optional;

public interface PropertyTypeService {
    List<PropertyType> getAllPropertyType();
    PropertyType getPropertyTypeById(int id);
    public PropertyType getPropertyTypeByName(String typeName);
}
