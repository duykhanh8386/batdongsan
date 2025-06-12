package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.employee.EmployeeResponse;
import com.BTL.Springboot.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<EmployeeDto> getAllEmployees();
    Employee getEmployeeById(Integer employeeId);

    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);

    List<EmployeeResponse> getAllEmployee();

    void deleteEmployee(int id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndNotId(String email, int id);

    void saveEmployee(Employee employee);

    void updateEmployee(Employee employee);
}
