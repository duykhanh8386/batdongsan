package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.service.ProjectService;
import com.BTL.Springboot.service.PropertyService;
import com.BTL.Springboot.service.PropertyTypeService;
import com.BTL.Springboot.service.UserAccountService;
import com.BTL.Springboot.util.ExcelExporterUtil;
import com.BTL.Springboot.util.PDFExporterUtil;
import jakarta.servlet.http.HttpServletResponse;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/project")
public class ApartmentController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyTypeService propertyTyperService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/apartments")
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
        model.addAttribute("project", new Project());
        model.addAttribute("propertyTypes", propertyTyperService.getAllPropertyType());
        model.addAttribute("apartments", projectService.getProjectByProjectTypeId(21));
       return "Apartment";
    }

    @GetMapping("/apartments/view/{id}")
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
        return "viewApartment";
    }

    @GetMapping("/apartments/edit/{id}")
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
        return "editApartment";
    }

    @PostMapping("/apartments/edit/{id}")
    public String updateProject(@PathVariable("id") Integer id,
                                Project updatedProject,
                                RedirectAttributes redirectAttributes) {
        projectService.updateProject(id, updatedProject);
        redirectAttributes.addFlashAttribute("successMessage", "Chỉnh sửa dự án thành công!");
        return "redirect:/project/apartments";
    }

    @PostMapping("/apartments/delete/{id}")
    public String softDeleteProject(@PathVariable("id") Integer id,RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sửa dự án thành công!");
        return "redirect:/project/apartments";
    }

    @PostMapping("/apartments/add")
    public String addProject(@ModelAttribute("project") Project project,
                             @ModelAttribute("projectType.typeId") Integer typeId,
                             RedirectAttributes redirectAttributes) {

        PropertyType propertyType = propertyTyperService.getPropertyTypeById(typeId);
        project.setProjectType(propertyType); // Gán thủ công

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm dự án thành công!");
        return "redirect:/project/apartments";
    }

    @GetMapping("/apartments/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=apartments.xlsx");

        List<Project> list = projectService.getProjectByTypeId(21);
        ExcelExporterUtil exporter = new ExcelExporterUtil(list);
        exporter.export(response);
    }

    @GetMapping("/apartments/export/pdf")
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=apartments.pdf");

        List<Project> list = projectService.getProjectByTypeId(21);
        PDFExporterUtil.export(response, list);
    }
    @PostMapping("/apartments/import/excel")
    public String importExcel(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage", "Vui lòng chọn một file Excel.");
                return "redirect:/project/apartments";
            }

            projectService.importFromExcel(file, "Căn hộ chung cư");
            redirectAttributes.addFlashAttribute("successMessage", "Import thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Import bằng file không thành công!!");
        }

        return "redirect:/project/apartments";
    }

    @PostMapping("/apartments/import/pdf")
    public String importPdf(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage", "Vui lòng chọn một file Pdf.");
                return "redirect:/project/apartments";
            }

            projectService.readProjectsFromPdf(file, "Căn hộ chung cư");
            redirectAttributes.addFlashAttribute("successMessage", "Import thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Import bằng file pdf không thành công!!");
        }

        return "redirect:/project/apartments";
    }

    @PostMapping("/apartments/import/image")
    public String uploadProjectFromImage(@RequestParam("file") MultipartFile file,
                                         RedirectAttributes redirectAttributes) throws TesseractException, IOException {
        // 1. OCR ảnh thành text
        String ocrText = projectService.extractTextFromImage(file);

        // 2. Lấy PropertyType mặc định
        PropertyType defaultType = propertyTyperService.getPropertyTypeById(21);

        try{
        // 3. Parse text thành Project
        Project project = projectService.parse(ocrText, defaultType);

        // 4. Lưu vào database
        projectService.saveProject(project);
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file Pdf.");
                return "redirect:/project/apartments";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Import thành công!");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Import bằng ảnh không thành công!!");
        }
        return "redirect:/project/apartments";
    }
}
