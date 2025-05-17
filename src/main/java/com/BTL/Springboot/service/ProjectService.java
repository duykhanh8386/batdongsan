package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;

import java.util.List;

public interface ProjectService {
    List<ProjectDto> getAllProjects();
    Project getProjectById(Integer projectId);
}
