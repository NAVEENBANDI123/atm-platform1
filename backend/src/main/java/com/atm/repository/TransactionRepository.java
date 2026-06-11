package com.atm.repository;

import com.atm.entity.Transaction;
import com.atm.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    List<Transaction> findTop5ByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t " +
           "where t.account.id = :accountId and t.type = :type and t.createdAt >= :since")
    BigDecimal sumByAccountAndTypeSince(@Param("accountId") Long accountId,
                                        @Param("type") TransactionType type,
                                        @Param("since") LocalDateTime since);
}
