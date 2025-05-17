package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.PropertyDto;
import com.BTL.Springboot.dto.request.PropertyRequest;
import com.BTL.Springboot.entity.Property;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    // Convert Property entity to PropertyDto
    public PropertyDto toDto(Property property) {
        if (property == null) {
            return null;
        }
        PropertyDto dto = new PropertyDto();
        dto.setPropertyId(property.getPropertyId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setAddress(property.getAddress());
        dto.setCity(property.getCity());
        dto.setState(property.getState());
        dto.setPrice(property.getPrice());
        dto.setArea(property.getArea());
        dto.setBedrooms(property.getBedrooms());
        dto.setBathrooms(property.getBathrooms());
        dto.setFloors(property.getFloors());
        dto.setYearBuilt(property.getYearBuilt());
        dto.setIsFurnished(property.getIsFurnished());
        dto.setListingType(property.getListingType());
        dto.setStatus(property.getStatus());
        dto.setOwner(property.getOwner());
        dto.setProject(property.getProject());
        dto.setListingAgent(property.getListingAgent());
        dto.setPropertyType(property.getPropertyType());
        dto.setPropertyCode(property.getPropertyCode());
        dto.setPostalCode(property.getPostalCode());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());
        return dto;
    }

    // Convert Property entity to PropertyDto
    public Property toEntity(PropertyDto dto) {
        if (dto == null) {
            return null;
        }
        Property property = new Property();
        property.setPropertyId(dto.getPropertyId());
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setAddress(dto.getAddress());
        property.setCity(dto.getCity());
        property.setState(dto.getState());
        property.setPrice(dto.getPrice());
        property.setArea(dto.getArea());
        property.setBedrooms(dto.getBedrooms());
        property.setBathrooms(dto.getBathrooms());
        property.setFloors(dto.getFloors());
        property.setYearBuilt(dto.getYearBuilt());
        property.setIsFurnished(dto.getIsFurnished());
        property.setListingType(dto.getListingType());
        property.setStatus(dto.getStatus());
        property.setOwner(dto.getOwner());
        property.setProject(dto.getProject());
        property.setListingAgent(dto.getListingAgent());
        property.setPropertyType(dto.getPropertyType());
        property.setPropertyCode(dto.getPropertyCode());
        property.setPostalCode(dto.getPostalCode());
        property.setCreatedAt(dto.getCreatedAt());
        property.setUpdatedAt(dto.getUpdatedAt());
        return property;
    }

    // Convert PropertyRequest to Property entity
    public Property toEntity(PropertyRequest request) {
        if (request == null) {
            return null;
        }
        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setPrice(request.getPrice());
        property.setArea(request.getArea());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setFloors(request.getFloors());
        property.setYearBuilt(request.getYearBuilt());
        property.setIsFurnished(request.getIsFurnished());
        property.setListingType(request.getListingType());
        property.setStatus(request.getStatus());
        property.setOwner(request.getOwner());
        property.setProject(request.getProject());
        property.setListingAgent(request.getListingAgent());
        property.setPropertyType(request.getPropertyType());
        property.setPropertyCode(request.getPropertyCode());
        property.setPostalCode(request.getPostalCode());
        property.setCreatedAt(request.getCreatedAt());
        property.setUpdatedAt(request.getUpdatedAt());
        return property;
    }

    // Convert PropertyDto to PropertyRequest
    public PropertyRequest toRequest(PropertyDto dto) {
        if (dto == null) {
            return null;
        }
        PropertyRequest request = new PropertyRequest();
        request.setPropertyCode(dto.getPropertyCode());
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setAddress(dto.getAddress());
        request.setCity(dto.getCity());
        request.setState(dto.getState());
        request.setPostalCode(dto.getPostalCode());
        request.setPrice(dto.getPrice());
        request.setArea(dto.getArea());
        request.setBedrooms(dto.getBedrooms());
        request.setBathrooms(dto.getBathrooms());
        request.setFloors(dto.getFloors());
        request.setYearBuilt(dto.getYearBuilt());
        request.setIsFurnished(dto.getIsFurnished());
        request.setListingType(dto.getListingType());
        request.setStatus(dto.getStatus());
        request.setOwner(dto.getOwner());
        request.setListingAgent(dto.getListingAgent());
        request.setPropertyType(dto.getPropertyType());
        request.setProject(dto.getProject());
        request.setCreatedAt(dto.getCreatedAt());
        request.setUpdatedAt(dto.getUpdatedAt());
        return request;
    }
}