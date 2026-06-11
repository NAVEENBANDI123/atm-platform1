package com.atm.repository;

import com.atm.entity.LoanScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanScheduleRepository extends JpaRepository<LoanScheduleEntry, Long> {

    List<LoanScheduleEntry> findByLoanAccountIdOrderByInstallmentNoAsc(Long loanAccountId);
}
