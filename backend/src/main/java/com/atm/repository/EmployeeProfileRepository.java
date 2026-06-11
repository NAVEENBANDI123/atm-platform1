package com.atm.repository;

import com.atm.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByUserId(Long userId);

    Optional<EmployeeProfile> findByEmployeeCode(String employeeCode);
}
