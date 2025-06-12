package com.BTL.Springboot.dto.response.employee;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Integer employeeId;
    private String firstName;
    private String lastName;
}
