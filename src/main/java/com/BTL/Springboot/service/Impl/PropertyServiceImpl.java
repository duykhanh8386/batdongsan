package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.repository.PropertyRepository;
import com.BTL.Springboot.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService {
    @Autowired
    private PropertyRepository propertyRepository;

    @Override
    public List<Property> findByProjectId(int id) {
        return propertyRepository.findByPropertyId(id);
    }

    @Override
    public List<Property> getPropertyByProjectId(int id) {
        return propertyRepository.findPropertiesByProjectId(id);
    }
}
