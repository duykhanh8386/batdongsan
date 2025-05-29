package com.BTL.Springboot.dto.response.project;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Integer projectId;
    private String projectName;
}
