package com.atm.repository;

import com.atm.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Beneficiary> findByOwnerIdAndAccountNumber(Long ownerId, String accountNumber);

    Optional<Beneficiary> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndAccountNumber(Long ownerId, String accountNumber);
}
