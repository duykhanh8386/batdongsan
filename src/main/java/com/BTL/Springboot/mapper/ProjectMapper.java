package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project project){
        if (project == null) {
            return null;
        }

        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(project.getProjectId());
        projectDto.setProjectName(project.getProjectName());
        projectDto.setDeveloper(project.getDeveloper());
        projectDto.setTotalArea(project.getTotalArea());
        projectDto.setLocation(project.getLocation());
        projectDto.setCity(project.getCity());
        projectDto.setState(project.getState());
        return projectDto;
    }

    public Project toEntity(ProjectDto dto) {
        if (dto == null) {
            return null;
        }

        Project project = new Project();
        project.setProjectId(dto.getId());
        project.setProjectName(dto.getProjectName());
        project.setDeveloper(dto.getDeveloper());
        project.setTotalArea(dto.getTotalArea());
        project.setLocation(dto.getLocation());
        project.setCity(dto.getCity());
        project.setState(dto.getState());
        return project;
    }
}
