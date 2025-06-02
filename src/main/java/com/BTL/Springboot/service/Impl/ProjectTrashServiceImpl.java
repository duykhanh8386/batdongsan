package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.entity.ProjectTrashBinEntity;
import com.BTL.Springboot.repository.ProjectTrashRepository;
import com.BTL.Springboot.service.ProjectTrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectTrashServiceImpl implements ProjectTrashService {

    @Autowired
    private ProjectTrashRepository projectTrashRepository;

    @Override
    public List<ProjectTrashBinEntity> getAll() {
        return projectTrashRepository.findAll();
    }

    @Scheduled(fixedRate = 1000 * 60 * 30)
    public void cleanExpiredTrash() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60);
        List<ProjectTrashBinEntity> expired = projectTrashRepository.findByDeletedAtBefore(threshold);

        for (ProjectTrashBinEntity trash : expired) {
            projectTrashRepository.delete(trash);
        }
    }
}
