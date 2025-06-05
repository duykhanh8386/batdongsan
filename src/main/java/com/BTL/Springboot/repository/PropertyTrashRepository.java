package com.BTL.Springboot.repository;

import com.BTL.Springboot.entity.PropertyTrashBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PropertyTrashRepository extends JpaRepository<PropertyTrashBin, Integer> {
    List<PropertyTrashBin> findByDeletedAtBefore(LocalDateTime time);
}
