package com.BTL.Springboot.dto.response.employee;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
    private int id;
    private String name;
    private String phone;
    private String email;
}
