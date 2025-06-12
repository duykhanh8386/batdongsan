package com.BTL.Springboot.converter;

import com.BTL.Springboot.dto.response.employee.EmployeeResponse;
import com.BTL.Springboot.entity.Employee;

public class Convert {
    public static EmployeeResponse EmployeeEntityToDto(Employee employee){
        EmployeeResponse employeeDto = new EmployeeResponse();
        employeeDto.setId(employee.getEmployeeId());
        employeeDto.setName(employee.getFirstName()+" "+employee.getLastName());
        employeeDto.setEmail(employee.getEmail());
        employeeDto.setPhone(employee.getPhone());
        return employeeDto;
    }
}
