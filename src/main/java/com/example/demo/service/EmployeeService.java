package com.example.demo.service;


import com.example.demo.dto.EmployeeDto;
import com.example.demo.entity.Employee;

import java.util.List;

public interface EmployeeService {
    public Employee getEmployeeById(int id);
    public List<EmployeeDto> getAllEmployee();
    public void deleteEmployee(int id);

    boolean existsByEmail(String email);

    void saveEmployee(Employee employee);

    boolean existsByEmailAndNotId(String email, int id);

    void updateEmployee(Employee employee);
}
