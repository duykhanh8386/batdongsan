package com.BTL.Springboot.dto;

import com.BTL.Springboot.entity.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    private Integer propertyId;
    private String propertyCode;
    private PropertyType propertyType;
    private Project project;
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
    private Customer owner;
    private Employee listingAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
