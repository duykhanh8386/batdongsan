package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;

public class Map {
    public static ProjectDto toProjectDto(Project project){
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
}
