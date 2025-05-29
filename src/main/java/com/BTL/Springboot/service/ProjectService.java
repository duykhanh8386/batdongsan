package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<ProjectDto> getAllProjects();
    Project getProjectById(Integer projectId);

    List<Project> findByProjectName(String projectName);
}
