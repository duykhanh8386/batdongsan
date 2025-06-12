package com.BTL.Springboot.dto.response.property;

import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    private Integer propertyId;
    private String propertyCode;
    private PropertyType propertyType;
    private String title;
    private String description;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private Double price;
    private Double area;
    private Byte bedrooms;
    private Byte bathrooms;
    private Byte floors;
    private Date yearBuilt;
    private Boolean isFurnished = false;
    private String listingType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectDto project;
    private CustomerDto owner;
    private EmployeeDto listingAgent;
}
