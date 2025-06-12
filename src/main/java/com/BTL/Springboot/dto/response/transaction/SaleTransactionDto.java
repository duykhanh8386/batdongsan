package com.BTL.Springboot.dto.response.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaleTransactionDto {
    private String customerName;
    private String propertyTitle;
    private Double price;
    private String status;
}
