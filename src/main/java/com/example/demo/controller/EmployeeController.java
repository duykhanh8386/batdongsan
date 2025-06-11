package com.example.demo.controller;

import com.example.demo.entity.Employee;
import com.example.demo.service.Impl.EmployeeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeServiceImpl employeeService;

    @GetMapping
    public String renderTemplate(Model model) {
        model.addAttribute("employeeList", employeeService.getAllEmployee());
        model.addAttribute("employee", new Employee());
        return "staff-profile";
    }

    @GetMapping("/view/{id}")
    public String viewEmployee(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        return "staff-view";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhân viên thành công!");
        return "redirect:/employee";
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            if (employee.getEmail() != null && employeeService.existsByEmail(employee.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email đã tồn tại!");
                redirectAttributes.addFlashAttribute("employee", employee);
                return "redirect:/employee";
            }
            employeeService.saveEmployee(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công!");
            return "redirect:/employee";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm nhân viên: " + e.getMessage());
            return "redirect:/employee";
        }
    }

    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable("id") Integer id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            model.addAttribute("errorMessage", "Nhân viên không tồn tại!");
            return "redirect:/employee";
        }
        model.addAttribute("employee", employee);
        return "edit-staff";
    }

    @PostMapping("/update/{id}")
    public String updateEmployee(@PathVariable("id") Integer id, @ModelAttribute Employee employee,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (employee.getEmail() != null &&
                    employeeService.existsByEmailAndNotId(employee.getEmail(), id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email đã được sử dụng bởi nhân viên khác!");
                redirectAttributes.addFlashAttribute("employee", employee);
                return "redirect:/employee/edit/" + id;
            }
            employee.setEmployeeId(id);
            System.out.println("Debug: updateEmployee, id = " + id + ", hireDate = " + employee.getHireDate());
            employeeService.updateEmployee(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
            return "redirect:/employee";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            return "redirect:/employee/edit/" + id;
        }
    }
}