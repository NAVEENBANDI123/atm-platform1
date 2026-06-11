package com.atm.repository;

import com.atm.entity.Complaint;
import com.atm.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Complaint> findByIdAndUserId(Long id, Long userId);

    Page<Complaint> findByStatusOrderByCreatedAtAsc(TicketStatus status, Pageable pageable);
}
