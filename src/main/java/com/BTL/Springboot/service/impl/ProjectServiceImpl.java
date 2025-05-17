package com.BTL.Springboot.service.impl;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.mapper.ProjectMapper;
import com.BTL.Springboot.repository.ProjectRepository;
import com.BTL.Springboot.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper mapper;

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Project getProjectById(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
    }
}
