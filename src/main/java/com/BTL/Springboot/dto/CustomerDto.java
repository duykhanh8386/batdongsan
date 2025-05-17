package com.BTL.Springboot.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private int customerId;
    private String firstName;
    private String lastName;
}
