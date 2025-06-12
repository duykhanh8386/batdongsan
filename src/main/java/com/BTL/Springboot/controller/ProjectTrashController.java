package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.service.Impl.ProjectServiceImpl;
import com.BTL.Springboot.service.Impl.ProjectTrashServiceImpl;
import com.BTL.Springboot.service.UserAccountService;
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
public class ProjectTrashController {

    @Autowired
    private ProjectTrashServiceImpl projectTrashService;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/trashs")
    public String navigate(Model model){
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            model.addAttribute("user", user != null ? user : new Object()); // Truyền object rỗng nếu null
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            model.addAttribute("user", new Object()); // Truyền object rỗng nếu có lỗi
        }
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
