package com.BTL.Springboot.dto.response.customer;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Integer customerId;
    private String firstName;
    private String lastName;
}
