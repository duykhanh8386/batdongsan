package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.converter.Convert;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.employee.EmployeeResponse;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.mapper.EmployeeMapper;
import com.BTL.Springboot.repository.EmployeeRepository;
import com.BTL.Springboot.service.EmployeeService;
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

    @Autowired
    private EmployeeMapper mapper;

    @Override
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Employee getEmployeeById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
    }

    @Override
    public List<Employee> findByFirstNameAndLastName(String firstName, String lastName) {
        return employeeRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    @Override
    public List<EmployeeResponse> getAllEmployee() {
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
