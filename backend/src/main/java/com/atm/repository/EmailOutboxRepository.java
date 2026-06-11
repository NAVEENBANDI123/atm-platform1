package com.atm.repository;

import com.atm.entity.EmailOutbox;
import com.atm.entity.EmailStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    List<EmailOutbox> findByStatusOrderByIdAsc(EmailStatus status, Pageable pageable);
}
