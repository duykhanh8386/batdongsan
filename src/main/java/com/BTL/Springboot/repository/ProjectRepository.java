package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByProjectName(String projectName);
}
