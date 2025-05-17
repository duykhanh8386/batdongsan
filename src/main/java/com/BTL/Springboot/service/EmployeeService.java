package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.EmployeeDto;
import com.BTL.Springboot.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<EmployeeDto> getAllEmployees();
    Employee getEmployeeById(Integer employeeId);
}
