package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Integer> {
    List<Property> findByPropertyId(Integer propertyId);
}
