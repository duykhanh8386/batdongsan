package com.example.demo.converter;

import com.example.demo.dto.EmployeeDto;
import com.example.demo.entity.Employee;

public class Convert {
    public static EmployeeDto EmployeeEntityToDto(Employee employee){
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setId(employee.getEmployeeId());
        employeeDto.setName(employee.getFirstName()+" "+employee.getLastName());
        employeeDto.setEmail(employee.getEmail());
        employeeDto.setPhone(employee.getPhone());
        return employeeDto;
    }
}
