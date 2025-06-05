package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.ProjectTrashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectTrashServiceImpl implements ProjectTrashService {

    @Autowired
    private ProjectTrashRepository projectTrashRepository;

    @Autowired
    private ProjectRepository projectRepository;

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
    public List<ProjectTrashBin> getAll() {
        return projectTrashRepository.findAll();
    }

    @Override
    public void restoreProject(Integer id) {
        Optional<ProjectTrashBin> projectTrashBinEntity = projectTrashRepository.findById(id);
        int projectId = projectTrashBinEntity.get().getProject().getProjectId();
        Project project = projectRepository.findById(projectId);
        project.setStatus("true");
        projectRepository.save(project);

        propertyRepository.restorePropertiesByProject(id);
        propertyImageRepository.restoreImagesByProject(id);
        appointmentRepository.restoreAppointmentsByProject(id);
        transactionRepository.restoreTransactionsByProject(id);
        paymentRepository.restorePaymentsByProject(id);

        projectTrashRepository.deleteById(id);
    }

    @Scheduled(fixedRate = 1000 * 60 * 30)
    public void cleanExpiredTrash() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60);
        List<ProjectTrashBin> expired = projectTrashRepository.findByDeletedAtBefore(threshold);

        for (ProjectTrashBin trash : expired) {
            projectTrashRepository.delete(trash);
        }
    }
}
