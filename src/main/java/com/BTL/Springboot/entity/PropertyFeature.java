package com.BTL.Springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "property_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFeature {
    @Id
    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Id
    @Column(name = "feature_name", nullable = false, length = 50)
    private String featureName;

    @Column(name = "feature_value", length = 100)
    private String featureValue;
}
