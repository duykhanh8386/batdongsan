package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    @Query("SELECT ua FROM UserAccount ua WHERE ua.username = :username")
    Optional<UserAccount> findByUsername(@Param("username") String username);
}
