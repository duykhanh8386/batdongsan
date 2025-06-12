package com.BTL.Springboot.dto.request.property;

import com.BTL.Springboot.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {
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
    private Project project;
    private Customer owner;
    private Employee listingAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
