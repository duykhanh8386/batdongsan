package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyFeatureRepository extends JpaRepository<PropertyFeature, String> {
}
