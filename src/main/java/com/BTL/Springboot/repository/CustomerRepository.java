package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);
}
