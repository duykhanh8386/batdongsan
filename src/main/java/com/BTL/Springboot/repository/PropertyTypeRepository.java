package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyTypeRepository extends JpaRepository<PropertyType,Integer> {
    PropertyType findById(int id);
    PropertyType findByTypeName(String typeName);
}
