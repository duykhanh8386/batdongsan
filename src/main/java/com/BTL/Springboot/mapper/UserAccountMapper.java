package com.BTL.Springboot.mapper;

import com.BTL.Springboot.dto.request.user.UserAccountRequest;
import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.role.RoleDto;
import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.entity.Role;
import com.BTL.Springboot.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {

    // Convert UserAccount entity to UserAccountDto
    public UserAccountDto toDto(UserAccount userAccount) {
        if (userAccount == null) {
            return null;
        }

        UserAccountDto dto = new UserAccountDto();
        dto.setUserId(userAccount.getUserId());
        dto.setUsername(userAccount.getUsername());
        dto.setPassword(userAccount.getPassword());
        dto.setEmail(userAccount.getEmail());

        // Ánh xạ Role
        if (userAccount.getRole() != null && userAccount.getRole().getRoleId() != null) {
            dto.setRole(new RoleDto(
                    userAccount.getRole().getRoleId(),
                    userAccount.getRole().getCode(),
                    userAccount.getRole().getRoleName(),
                    userAccount.getRole().getDescription()
            ));
        } else {
            dto.setRole(null);
        }

        // Ánh xạ Customer
        if (userAccount.getCustomer() != null && userAccount.getCustomer().getCustomerId() != null) {
            dto.setCustomer(new CustomerDto(
                    userAccount.getCustomer().getCustomerId(),
                    userAccount.getCustomer().getFirstName(),
                    userAccount.getCustomer().getLastName()
            ));
        } else {
            dto.setCustomer(null);
        }

        // Ánh xạ Employee
        if (userAccount.getEmployee() != null && userAccount.getEmployee().getEmployeeId() != null) {
            dto.setEmployee(new EmployeeDto(
                    userAccount.getEmployee().getEmployeeId(),
                    userAccount.getEmployee().getFirstName(),
                    userAccount.getEmployee().getLastName()
            ));
        } else {
            dto.setEmployee(null);
        }

        return dto;
    }

    // Convert UserAccountDto to UserAccount entity
    public UserAccount toEntity(UserAccountDto dto) {
        if (dto == null) {
            return null;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(dto.getUserId());
        userAccount.setUsername(dto.getUsername());
        userAccount.setPassword(dto.getPassword());
        userAccount.setEmail(dto.getEmail());

        // Ánh xạ Role
        if (dto.getRole() != null && dto.getRole().getRoleId() != null) {
            Role role = new Role();
            role.setRoleId(dto.getRole().getRoleId());
            userAccount.setRole(role);
        } else {
            userAccount.setRole(null);
        }

        // Ánh xạ Customer
        if (dto.getCustomer() != null && dto.getCustomer().getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setCustomerId(dto.getCustomer().getCustomerId());
            userAccount.setCustomer(customer);
        } else {
            userAccount.setCustomer(null);
        }

        // Ánh xạ Employee
        if (dto.getEmployee() != null && dto.getEmployee().getEmployeeId() != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(dto.getEmployee().getEmployeeId());
            userAccount.setEmployee(employee);
        } else {
            userAccount.setEmployee(null);
        }

        return userAccount;
    }

    // Convert UserAccountRequest to UserAccount entity
    public UserAccount toEntity(UserAccountRequest request) {
        if (request == null) {
            return null;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername());
        userAccount.setPassword(request.getPassword());
        userAccount.setEmail(request.getEmail());
        userAccount.setRole(request.getRole());
        userAccount.setEmployee(request.getEmployee());
        userAccount.setCustomer(request.getCustomer());

        return userAccount;
    }

    // Convert UserAccountDto to UserAccountRequest
    public UserAccountRequest toRequest(UserAccountDto dto) {
        if (dto == null) {
            return null;
        }

        UserAccountRequest request = new UserAccountRequest();
        request.setUsername(dto.getUsername());
        request.setPassword(dto.getPassword());
        request.setEmail(dto.getEmail());

        // Ánh xạ Role
        if (dto.getRole() != null && dto.getRole().getRoleId() != null) {
            Role role = new Role();
            role.setRoleId(dto.getRole().getRoleId());
            request.setRole(role);
        } else {
            request.setRole(null);
        }

        // Ánh xạ Customer
        if (dto.getCustomer() != null && dto.getCustomer().getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setCustomerId(dto.getCustomer().getCustomerId());
            request.setCustomer(customer);
        } else {
            request.setCustomer(null);
        }

        // Ánh xạ Employee
        if (dto.getEmployee() != null && dto.getEmployee().getEmployeeId() != null) {
            Employee employee = new Employee();
            employee.setEmployeeId(dto.getEmployee().getEmployeeId());
            request.setEmployee(employee);
        } else {
            request.setEmployee(null);
        }

        return request;
    }
}