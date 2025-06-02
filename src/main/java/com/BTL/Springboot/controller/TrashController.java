package com.BTL.Springboot.controller;

import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBinEntity;
import com.BTL.Springboot.service.Impl.ProjectTrashServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/project")
public class TrashController {

    @Autowired
    private ProjectTrashServiceImpl projectTrashService;

    @GetMapping("/trashs")
    public String navigate(Model model){
        model.addAttribute("trash", new ProjectTrashBinEntity());
        model.addAttribute("trashList", projectTrashService.getAll());
        return "Trash";
    }
}
