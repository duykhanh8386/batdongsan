package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.repository.PropertyTypeRepository;
import com.BTL.Springboot.service.PropertyTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyTypeServiceImpl implements PropertyTypeService {
    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Override
    public List<PropertyType> getAllPropertyType() {
        return propertyTypeRepository.findAll();
    }

    @Override
    public PropertyType getPropertyTypeById(int id) {
        return propertyTypeRepository.findById(id);
    }

    @Override
    public PropertyType getPropertyTypeByName(String typeName) {
        return propertyTypeRepository.findByTypeName(typeName);
    }
}
