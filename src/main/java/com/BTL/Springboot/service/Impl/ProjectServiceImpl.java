package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBinEntity;
import com.BTL.Springboot.mapper.Map;
import com.BTL.Springboot.repository.ProjectRepository;
import com.BTL.Springboot.repository.ProjectTrashRepository;
import com.BTL.Springboot.service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectTrashRepository projectTrashRepository;

    @Override
    public List<ProjectDto> getProjectByProjectTypeId(int typeId) {
        List<ProjectDto> list = projectRepository.findByProjectTypeIdAndNotDeleted(typeId)
                .stream()
                .map(Map::toProjectDto)
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public Project getProjectById(int id){
        return projectRepository.findById(id);
    }

    @Override
    public void updateProject(int id,Project updatedProject) {
        Project project = projectRepository.findById(id);
        project.setProjectName(updatedProject.getProjectName());
        project.setProjectType(updatedProject.getProjectType());
        project.setDeveloper(updatedProject.getDeveloper());
        project.setLocation(updatedProject.getLocation());
        project.setCity(updatedProject.getCity());
        project.setState(updatedProject.getState());
        project.setTotalArea(updatedProject.getTotalArea());
        project.setTotalUnits(updatedProject.getTotalUnits());
        project.setStartDate(updatedProject.getStartDate());
        project.setCompletionDate(updatedProject.getCompletionDate());
        project.setStatus(updatedProject.getStatus());
        project.setDescription(updatedProject.getDescription());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    public void deleteProject(int id) {
        Project project = projectRepository.findById(id);
        project.setStatus("false");
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
        ProjectTrashBinEntity projectTrashBinEntity = new ProjectTrashBinEntity();
        projectTrashBinEntity.setProject(project);
        projectTrashBinEntity.setDeletedAt(LocalDateTime.now());
        projectTrashRepository.save(projectTrashBinEntity);
    }

    @Override
    @Transactional
    public void saveProject(Project project) {
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }
}
