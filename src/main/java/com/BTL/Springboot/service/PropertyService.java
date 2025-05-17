package com.BTL.Springboot.service;

import com.BTL.Springboot.entity.Property;

import java.util.List;

public interface PropertyService {
    List<Property> findByProjectId(int id);
}
