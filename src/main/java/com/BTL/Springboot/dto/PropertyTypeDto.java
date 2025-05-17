package com.BTL.Springboot.dto;
import com.BTL.Springboot.entity.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeDto {
    private int typeId;
    private String typeName;

}
