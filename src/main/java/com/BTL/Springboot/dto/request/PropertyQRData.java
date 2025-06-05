package com.BTL.Springboot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyQRData {
    private String propertyCode;
    private Integer propertyTypeId;
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
    private String yearBuilt;
    private Boolean isFurnished;
    private String listingType;
    private String status;
    private Integer projectId;
    private Integer ownerId;
    private Integer listingAgentId;
    private String createdAt;
    private String updatedAt;
}
