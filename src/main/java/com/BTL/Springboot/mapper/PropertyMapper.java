package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.entity.*;
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
        dto.setIsFurnished(property.getIsFurnished() != null ? property.getIsFurnished() : false);
        dto.setListingType(property.getListingType());
        dto.setStatus(property.getStatus());
        dto.setPropertyType(property.getPropertyType() != null ? property.getPropertyType() : new PropertyType());
        dto.setPropertyCode(property.getPropertyCode());
        dto.setPostalCode(property.getPostalCode());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());

        // Ánh xạ Project
        if (property.getProject() != null && property.getProject().getProjectId() != null) {
            dto.setProject(new ProjectDto(
                    property.getProject().getProjectId(),
                    property.getProject().getProjectName()
            ));
        } else {
            dto.setProject(null);
        }

        // Ánh xạ Customer (Owner)
        if (property.getOwner() != null && property.getOwner().getCustomerId() != null) {
            dto.setOwner(new CustomerDto(
                    property.getOwner().getCustomerId(),
                    property.getOwner().getFirstName(),
                    property.getOwner().getLastName()
            ));
        } else {
            dto.setOwner(null);
        }

        // Ánh xạ Employee (ListingAgent)
        if (property.getListingAgent() != null && property.getListingAgent().getEmployeeId() != null) {
            dto.setListingAgent(new EmployeeDto(
                    property.getListingAgent().getEmployeeId(),
                    property.getListingAgent().getFirstName(),
                    property.getListingAgent().getLastName()
            ));
        } else {
            dto.setListingAgent(null);
        }

        return dto;
    }

    // Convert PropertyDto to Property entity
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
        property.setPropertyType(dto.getPropertyType());
        property.setPropertyCode(dto.getPropertyCode());
        property.setPostalCode(dto.getPostalCode());
        property.setCreatedAt(dto.getCreatedAt());
        property.setUpdatedAt(dto.getUpdatedAt());

        // Ánh xạ Project
        if (dto.getProject() != null && dto.getProject().getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(dto.getProject().getProjectId());
            property.setProject(project);
        } else {
            property.setProject(null);
        }

        // Ánh xạ Customer
        if (dto.getOwner() != null && dto.getOwner().getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setCustomerId(dto.getOwner().getCustomerId());
            property.setOwner(customer);
        } else {
            property.setOwner(null);
        }

        // Ánh xạ Employee
        if (dto.getListingAgent() != null && dto.getListingAgent().getEmployeeId() != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(dto.getListingAgent().getEmployeeId());
            property.setListingAgent(employee);
        } else {
            property.setListingAgent(null);
        }

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
        property.setPropertyType(request.getPropertyType());
        property.setPropertyCode(request.getPropertyCode());
        property.setPostalCode(request.getPostalCode());
        property.setCreatedAt(request.getCreatedAt());
        property.setUpdatedAt(request.getUpdatedAt());

        // Ánh xạ Project
        if (request.getProject() != null && request.getProject().getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(request.getProject().getProjectId());
            property.setProject(project);
        } else {
            property.setProject(null);
        }

        // Ánh xạ Customer
        if (request.getOwner() != null && request.getOwner().getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setCustomerId(request.getOwner().getCustomerId());
            property.setOwner(customer);
        } else {
            property.setOwner(null);
        }

        // Ánh xạ Employee
        if (request.getListingAgent() != null && request.getListingAgent().getEmployeeId() != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(request.getListingAgent().getEmployeeId());
            property.setListingAgent(employee);
        } else {
            property.setListingAgent(null);
        }

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
        request.setPropertyType(dto.getPropertyType());
        request.setCreatedAt(dto.getCreatedAt());
        request.setUpdatedAt(dto.getUpdatedAt());

        // Ánh xạ Project
        if (dto.getProject() != null && dto.getProject().getProjectId() != null) {
            Project project = new Project();
            project.setProjectId(dto.getProject().getProjectId());
            request.setProject(project);
        } else {
            request.setProject(null);
        }

        // Ánh xạ Owner
        if (dto.getOwner() != null && dto.getOwner().getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setCustomerId(dto.getOwner().getCustomerId());
            request.setOwner(customer);
        } else {
            request.setOwner(null);
        }

        // Ánh xạ ListingAgent
        if (dto.getListingAgent() != null && dto.getListingAgent().getEmployeeId() != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(dto.getListingAgent().getEmployeeId());
            request.setListingAgent(employee);
        } else {
            request.setListingAgent(null);
        }

        return request;
    }
}