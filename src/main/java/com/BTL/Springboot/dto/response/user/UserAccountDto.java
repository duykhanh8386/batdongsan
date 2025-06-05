package com.BTL.Springboot.dto.response.user;

import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.role.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDto {
    private Integer userId;
    private String username;
    private String password;
    private String email;
    private RoleDto role;
    private CustomerDto customer;
    private EmployeeDto employee;
}
