package com.BTL.Springboot.service;

import com.BTL.Springboot.entity.ProjectTrashBin;

import java.util.List;

public interface ProjectTrashService {
    public List<ProjectTrashBin> getAll();
    public void restoreProject(Integer id);
}
