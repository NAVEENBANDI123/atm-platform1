package com.atm.repository;

import com.atm.entity.ApplicationStatus;
import com.atm.entity.LoanApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    Page<LoanApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status, Pageable pageable);

    Page<LoanApplication> findByStatusInOrderByCreatedAtAsc(List<ApplicationStatus> statuses, Pageable pageable);

    Page<LoanApplication> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
