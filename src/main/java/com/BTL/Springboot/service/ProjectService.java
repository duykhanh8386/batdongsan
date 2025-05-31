package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;

import java.util.List;

public interface ProjectService {
    public List<ProjectDto> getProjectByProjectTypeId(int typeId);
    public Project getProjectById(int id);
    public void updateProject(int id,Project updatedProject);
    public void deleteProject(int id);
    public void saveProject(Project project);
    public List<Project> getAll();
}
