package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
