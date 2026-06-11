package com.atm.repository;

import com.atm.entity.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Long> {

    List<Nominee> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    Optional<Nominee> findByIdAndAccountId(Long id, Long accountId);
}
