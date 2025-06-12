package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT COUNT(c) FROM Customer c WHERE YEAR(c.createdAt) = :year")
    long countByYear(@Param("year") int year);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :start AND :end")
    Long getNewCustomersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
