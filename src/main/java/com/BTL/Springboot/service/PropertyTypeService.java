package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.dto.PropertyTypeDto;
import com.BTL.Springboot.entity.PropertyType;

import java.util.List;

public interface PropertyTypeService {
    List<PropertyTypeDto> getAllPropertyTypes();
    PropertyType getPropertyTypeById(Integer typeId);
}
