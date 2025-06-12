package com.BTL.Springboot.dto.request.property;

import com.BTL.Springboot.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTrashRequest {
    private Property property;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
