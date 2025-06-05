package com.BTL.Springboot.dto.request.user;

import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountRequest {
    private String username;
    private String password;
    private String email;
    private Role role;
    private Employee employee;
    private Customer customer;
}
