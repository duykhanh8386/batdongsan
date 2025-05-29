package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDto dto = new ProjectDto();
        dto.setProjectId(project.getProjectId());
        dto.setProjectName(project.getProjectName());
        return dto;
    }
}
