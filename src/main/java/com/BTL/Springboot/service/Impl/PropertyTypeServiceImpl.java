package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.property_type.PropertyTypeDto;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.mapper.PropertyTypeMapper;
import com.BTL.Springboot.repository.PropertyTypeRepository;
import com.BTL.Springboot.service.PropertyTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyTypeServiceImpl implements PropertyTypeService {

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private PropertyTypeMapper mapper;

    @Override
    public List<PropertyType> getAllPropertyType() {
        return propertyTypeRepository.findAll();
    }

    @Override
    public List<PropertyTypeDto> getAllPropertyTypes() {
        return propertyTypeRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PropertyType getPropertyTypeById(Integer typeId) {
        return propertyTypeRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("PropertyType not found with id: " + typeId));
    }

    @Override
    public PropertyType getPropertyTypeByName(String typeName) {
        return propertyTypeRepository.findByTypeName(typeName);
    }
}
