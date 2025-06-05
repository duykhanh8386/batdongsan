package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProjectService {
    List<ProjectDto> getAllProjects();
    Project getProjectById(Integer projectId);

    List<Project> findByProjectName(String projectName);

    public List<ProjectDto> getProjectByProjectTypeId(int typeId);
    public void updateProject(int id,Project updatedProject);
    public void deleteProject(int id);
    public void saveProject(Project project);
    public List<Project> getAll();

    public List<Project> getProjectByTypeId(int typeId);
    public Project getProjectById(int id);
    public void importFromExcel(MultipartFile file) throws IOException;
    public void readProjectsFromPdf(MultipartFile file)throws IOException;
}
