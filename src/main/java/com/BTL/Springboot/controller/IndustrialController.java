package com.BTL.Springboot.controller;

import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.service.Impl.ProjectServiceImpl;
import com.BTL.Springboot.service.Impl.PropertyServiceImpl;
import com.BTL.Springboot.service.Impl.PropertyTypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/project")
public class IndustrialController {
    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private PropertyServiceImpl propertyService;

    @Autowired
    private PropertyTypeServiceImpl propertyTyperService;

    @GetMapping("/industrial-zones")
    public String navigate3(Model model){
        model.addAttribute("project", new Project());
        model.addAttribute("propertyTypes", propertyTyperService.getAllPropertyType());
        model.addAttribute("industrials", projectService.getProjectByProjectTypeId(24));
        return "Industrial-zones";
    }


    @GetMapping("/industrial-zones/view/{id}")
    public String viewProject(@PathVariable("id") Integer id, Model model) {
        // Tìm project theo ID
        Project project = projectService.getProjectById(id);
        List<Property> properties = propertyService.getPropertyByProjectId(id);
        model.addAttribute("project", project);
        model.addAttribute("properties", properties);
        return "viewIndustrial";
    }

    @GetMapping("/industrial-zones/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Project project = projectService.getProjectById(id);

        List<PropertyType> propertyTypes = propertyTyperService.getAllPropertyType();

        model.addAttribute("project", project);
        model.addAttribute("propertyTypes", propertyTypes);
        return "editindustrial"; // Trả về template biểu mẫu chỉnh sửa
    }

    @PostMapping("/industrial-zones/edit/{id}")
    public String updateProject(@PathVariable("id") Integer id,
                                Project updatedProject,
                                RedirectAttributes redirectAttributes) {
        projectService.updateProject(id, updatedProject);
        redirectAttributes.addFlashAttribute("successMessage", "Chỉnh sửa dự án thành công!");
        return "redirect:/project/industrial-zones";
    }

    @PostMapping("/industrial-zones/delete/{id}")
    public String softDeleteProject(@PathVariable("id") Integer id,RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sửa dự án thành công!");
        return "redirect:/project/industrial-zones";
    }

//    @GetMapping("/industrial-zones/add")
//    public String showAddForm(Model model) {
//        Project newProject = new Project();
//        List<PropertyType> propertyTypes = propertyTyperService.getAllPropertyType();
//
//        model.addAttribute("project", newProject);
//        model.addAttribute("propertyTypes", propertyTypes);
//
//        return "addIndustrial"; // Tên file HTML thêm mới
//    }

    @PostMapping("/industrial-zones/add")
    public String addProject(@ModelAttribute("project") Project project,
                             @ModelAttribute("projectType.typeId") Integer typeId,
                             RedirectAttributes redirectAttributes) {

        // Lấy propertyType từ ID
        PropertyType propertyType = propertyTyperService.getPropertyTypeById(typeId);
        project.setProjectType(propertyType);

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm dự án thành công!");
        return "redirect:/project/industrial-zones";
    }
}
