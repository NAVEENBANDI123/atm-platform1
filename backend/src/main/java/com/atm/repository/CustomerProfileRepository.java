package com.atm.repository;

import com.atm.entity.CustomerProfile;
import com.atm.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByUserId(Long userId);

    Optional<CustomerProfile> findByCustomerId(String customerId);

    Optional<CustomerProfile> findByUserUsername(String username);

    boolean existsByAadhaar(String aadhaar);

    boolean existsByPan(String pan);

    Page<CustomerProfile> findByCustomerStatus(CustomerStatus status, Pageable pageable);
}
