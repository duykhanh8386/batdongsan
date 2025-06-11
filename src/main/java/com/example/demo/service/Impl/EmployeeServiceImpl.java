package com.example.demo.service.Impl;

import com.example.demo.converter.Convert;
import com.example.demo.dto.EmployeeDto;
import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee getEmployeeById(int id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        return employee;
    }

    @Override
    public List<EmployeeDto> getAllEmployee() {
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(Convert::EmployeeEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(int id) {
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee != null) {
            employee.setIsActive(false);
            employeeRepository.save(employee);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndNotId(String email, int id) {
        return employeeRepository.existsByEmailAndEmployeeIdNot(email, id);
    }

    @Override
    public void saveEmployee(Employee employee) {
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setIsActive(true);

        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        if (employee.getManager() != null && employee.getManager().getEmployeeId() != null) {
            Employee manager = employeeRepository.findById(employee.getManager().getEmployeeId())
                    .orElse(null);
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        employeeRepository.save(employee);
    }

    @Override
    public void updateEmployee(Employee employee) {
        Employee existingEmployee = employeeRepository.findById(employee.getEmployeeId()).orElse(null);
        if (existingEmployee != null) {
            existingEmployee.setFirstName(employee.getFirstName());
            existingEmployee.setLastName(employee.getLastName());
            existingEmployee.setPhone(employee.getPhone());
            existingEmployee.setEmail(employee.getEmail());
            existingEmployee.setHireDate(employee.getHireDate());
            existingEmployee.setPosition(employee.getPosition());
            existingEmployee.setSalary(employee.getSalary());
            existingEmployee.setUpdatedAt(LocalDateTime.now());

            if (employee.getManager() != null && employee.getManager().getEmployeeId() != null) {
                Employee manager = employeeRepository.findById(employee.getManager().getEmployeeId())
                        .orElse(null);
                existingEmployee.setManager(manager);
            } else {
                existingEmployee.setManager(null);
            }
            employeeRepository.save(existingEmployee);
        }
    }
}