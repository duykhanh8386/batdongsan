package com.BTL.Springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "property_code", nullable = false, unique = true, length = 20)
    private String propertyCode;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "property_type_id", nullable = false)
    private PropertyType propertyType;

    @JsonIgnore
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
    private Date yearBuilt;

    @Column(name = "is_furnished", nullable = false)
    private Boolean isFurnished = false;

    @JsonIgnore
    @Column(name = "listing_type", nullable = false, length = 50)
    private String listingType;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Customer owner;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "listing_agent_id", nullable = false)
    private Employee listingAgent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<PropertyFeature> features;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<PropertyImage> images;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<Transaction> transactions;

    @JsonIgnore
    @OneToMany(mappedBy = "property")
    private List<Appointment> appointments;
}
