package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.mapper.Map;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(Map::toProjectDto).collect(Collectors.toList());
    }

    @Override
    public Project getProjectById(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
    }

    @Override
    public List<Project> findByProjectName(String projectName) {
        return projectRepository.findByProjectName(projectName);
    }



    @Override
    public List<ProjectDto> getProjectByProjectTypeId(int typeId) {
        List<ProjectDto> list = projectRepository.findByProjectTypeIdAndNotDeleted(typeId)
                .stream()
                .map(Map::toProjectDto)
                .collect(Collectors.toList());
        return list;
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

        propertyRepository.softDeletePropertiesByProject(id);
        propertyImageRepository.softDeleteImagesByProject(id);
        appointmentRepository.cancelAppointmentsByProject(id);
        transactionRepository.cancelTransactionsByProject(id);
        paymentRepository.cancelPaymentsByProject(id);

        ProjectTrashBin projectTrashBinEntity = new ProjectTrashBin();
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
