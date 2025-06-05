package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.service.Impl.ProjectServiceImpl;
import com.BTL.Springboot.service.Impl.PropertyServiceImpl;
import com.BTL.Springboot.service.Impl.PropertyTypeServiceImpl;
import com.BTL.Springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/project")
public class HouseController {
    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private PropertyServiceImpl propertyService;

    @Autowired
    private PropertyTypeServiceImpl propertyTyperService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/houses")
    public String navigate4(Model model){
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
        model.addAttribute("project", new Project());
        model.addAttribute("propertyTypes", propertyTyperService.getAllPropertyType());
        List<ProjectDto> houses = projectService.getProjectByProjectTypeId(23);
        model.addAttribute("houses", houses);
        return "House";
    }

    @GetMapping("/houses/view/{id}")
    public String viewProject(@PathVariable("id") Integer id, Model model) {
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
        // Tìm project theo ID
        Project project = projectService.getProjectById(id);
        List<Property> properties = propertyService.getPropertyByProjectId(id);
        model.addAttribute("project", project);
        model.addAttribute("properties", properties);
        return "viewHouse";
    }

    @GetMapping("/houses/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
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
        Project project = projectService.getProjectById(id);

        List<PropertyType> propertyTypes = propertyTyperService.getAllPropertyType();

        model.addAttribute("project", project);
        model.addAttribute("propertyTypes", propertyTypes);
        return "editHouse"; // Trả về template biểu mẫu chỉnh sửa
    }

    @PostMapping("/houses/edit/{id}")
    public String updateProject(@PathVariable("id") Integer id, Project updatedProject,
                                RedirectAttributes redirectAttributes) {
        projectService.updateProject(id,updatedProject);
        redirectAttributes.addFlashAttribute("successMessage", "Chỉnh sửa dự án thành công!");
        return "redirect:/project/houses";
    }

    @PostMapping("/houses/delete/{id}")
    public String softDeleteProject(@PathVariable("id") Integer id,RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sửa dự án thành công!");
        return "redirect:/project/houses";
    }

//    @GetMapping("/houses/add")
//    public String showAddForm(Model model) {
//        Project newProject = new Project();
//        List<PropertyType> propertyTypes = propertyTyperService.getAllPropertyType();
//
//        model.addAttribute("project", newProject);
//        model.addAttribute("propertyTypes", propertyTypes);
//
//        return "addHouse"; // Tên file HTML thêm mới
//    }

    @PostMapping("/houses/add")
    public String addProject(@ModelAttribute("project") Project project,
                             @ModelAttribute("projectType.typeId") Integer typeId,
                             RedirectAttributes redirectAttributes) {

        // Lấy propertyType từ ID
        PropertyType propertyType = propertyTyperService.getPropertyTypeById(typeId);
        project.setProjectType(propertyType);

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm dự án thành công!");
        return "redirect:/project/houses";
    }
}
