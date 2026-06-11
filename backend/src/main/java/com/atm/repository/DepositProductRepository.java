package com.atm.repository;

import com.atm.entity.DepositProduct;
import com.atm.entity.DepositType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {

    List<DepositProduct> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<DepositProduct> findByUserIdAndDepositTypeOrderByCreatedAtDesc(Long userId, DepositType depositType);
}
