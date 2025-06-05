package com.BTL.Springboot.dto.response.property;

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
public class PropertyTrashDto {
    private Integer id;
    private Property property;
    private LocalDateTime deletedAt;
}
