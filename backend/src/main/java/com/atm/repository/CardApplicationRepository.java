package com.atm.repository;

import com.atm.entity.ApplicationStatus;
import com.atm.entity.CardApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardApplicationRepository extends JpaRepository<CardApplication, Long> {

    Page<CardApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status, Pageable pageable);

    Page<CardApplication> findByStatusInOrderByCreatedAtAsc(List<ApplicationStatus> statuses, Pageable pageable);

    Page<CardApplication> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<CardApplication> findByUserIdAndStatusIn(Long userId, List<ApplicationStatus> statuses);
}
