package com.atm.repository;

import com.atm.entity.User;
import com.atm.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndMobile(String username, String mobile);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    Page<User> findByUserType(UserType userType, Pageable pageable);
}
