package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
