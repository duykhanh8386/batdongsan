package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Integer> {
    List<PropertyImage> findByProperty(Property property);
    PropertyImage findByImageUrl(String imageUrl);
}