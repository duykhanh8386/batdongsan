package com.example.demo.repository;

import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee,Integer>{
    List<Employee> findByIsActiveTrue();
    boolean existsByEmail(String email);
    boolean existsByEmailAndEmployeeIdNot(String email,Integer id);
}
