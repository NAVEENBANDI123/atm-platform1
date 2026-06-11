package com.atm.repository;

import com.atm.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    Optional<LoanAccount> findByLoanAccountNo(String loanAccountNo);

    List<LoanAccount> findByAccountUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByLoanAccountNo(String loanAccountNo);
}
