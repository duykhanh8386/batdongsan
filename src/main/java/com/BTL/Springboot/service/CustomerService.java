package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.CustomerDto;
import com.BTL.Springboot.entity.Customer;

import java.util.List;

public interface CustomerService {

    List<CustomerDto> getAllCustomer();
    Customer getCustomerById(Integer customerId);
}
