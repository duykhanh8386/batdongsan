package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "property_code", nullable = false, unique = true, length = 20)
    private String propertyCode;

    @ManyToOne
    @JoinColumn(name = "property_type_id", nullable = false)
    private PropertyType propertyType;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "area", nullable = false)
    private Double area;

    @Column(name = "bedrooms")
    private Byte bedrooms;

    @Column(name = "bathrooms")
    private Byte bathrooms;

    @Column(name = "floors")
    private Byte floors;

    @Column(name = "year_built")
    private LocalDate yearBuilt;

    @Column(name = "is_furnished", nullable = false)
    private Boolean isFurnished = false;

    @Column(name = "listing_type", nullable = false, length = 50)
    private String listingType;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "available";

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Customer owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "listing_agent_id", nullable = false)
    private Employee listingAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<PropertyFeature> features;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<PropertyImage> images;

    @JsonIgnore
    @OneToMany(mappedBy = "property" )
    private List<Transaction> transactions;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<Appointment> appointments;
}
