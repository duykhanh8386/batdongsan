package com.BTL.Springboot.controller;

import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.service.Impl.ProjectServiceImpl;
import com.BTL.Springboot.service.Impl.ProjectTrashServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/project")
public class TrashController {

    @Autowired
    private ProjectTrashServiceImpl projectTrashService;

    @Autowired
    private ProjectServiceImpl projectService;

    @GetMapping("/trashs")
    public String navigate(Model model){
        model.addAttribute("trash", new ProjectTrashBin());
        model.addAttribute("trashList", projectTrashService.getAll());
        return "Trash";
    }

    @PostMapping("/restore/{id}")
    public String restoreProject(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("successMessage", "Khôi phục dự án thành công!");
        projectTrashService.restoreProject(id);
        return "redirect:/project/trashs";
    }
}
