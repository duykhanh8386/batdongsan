package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.ProjectTrashBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectTrashRepository extends JpaRepository<ProjectTrashBin,Integer> {
    List<ProjectTrashBin> findByDeletedAtBefore(LocalDateTime time);
}
