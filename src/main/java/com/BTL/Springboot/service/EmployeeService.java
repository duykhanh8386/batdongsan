package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<EmployeeDto> getAllEmployees();
    Employee getEmployeeById(Integer employeeId);

    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
}
