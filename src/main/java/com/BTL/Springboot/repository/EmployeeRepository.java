package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
    boolean existsByEmail(String email);

    List<Employee> findByIsActiveTrue();
    boolean existsByEmailAndEmployeeIdNot(String email,Integer id);
}
