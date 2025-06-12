package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.property.PropertyTrashDto;

import java.util.Map;

public interface PropertyTrashService {

    PropertyTrashDto getDeletedProperty(Integer id);
    void restoreProperty(Integer id);
    Map<String, Object> findAllDeletedPropertiesWithDetails();
}
