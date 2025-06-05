package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    List<CustomerDto> getAllCustomer();
    Customer getCustomerById(Integer customerId);
    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);
}
