package com.BTL.Springboot.dto.response.property_image;

import com.BTL.Springboot.entity.Property;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImageDto {
    private Integer imageId;
    private Property property;
    private String imageUrl;
    private String caption;
    private Boolean isMain = false;
    private LocalDateTime createdAt;
}
