package com.BTL.Springboot.service.impl;

import com.BTL.Springboot.dto.CustomerDto;
import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.mapper.CustomerMapper;
import com.BTL.Springboot.repository.CustomerRepository;
import com.BTL.Springboot.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper mapper;

    @Override
    public List<CustomerDto> getAllCustomer() {
        return customerRepository.findAll().stream()
                .map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Customer getCustomerById(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
    }
}
