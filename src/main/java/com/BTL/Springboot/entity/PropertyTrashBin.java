package com.BTL.Springboot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "properties_trash_bin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTrashBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}