package com.BTL.Springboot.dto.response.property_type;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeDto {
    private int typeId;
    private String typeName;

}
