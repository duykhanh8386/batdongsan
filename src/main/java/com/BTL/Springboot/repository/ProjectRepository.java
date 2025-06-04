package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    List<Project> findByProjectName(String projectName);

    @Query("SELECT p FROM Project p WHERE p.status <> 'false' AND p.projectType.typeId = :typeId")
    List<Project> findByProjectTypeIdAndNotDeleted(@Param("typeId") int typeId);

    Project findById(int id);
}
