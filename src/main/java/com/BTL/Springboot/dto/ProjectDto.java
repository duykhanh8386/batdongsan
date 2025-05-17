package com.BTL.Springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Integer id;
    private String projectName;
    private String developer;
    private String location;
    private String city;
    private String state;
    private Double totalArea;
}
