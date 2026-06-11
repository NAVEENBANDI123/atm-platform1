package com.atm.mapper;

import com.atm.dto.response.UserResponse;
import com.atm.entity.Role;
import com.atm.entity.User;
import com.atm.repository.CustomerProfileRepository;
import com.atm.repository.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hand-written mapper (rather than MapStruct) so we can fold the
 * customer/employee profile join in without an extra mapper class.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CustomerProfileRepository customerProfileRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        String customerId = customerProfileRepository.findByUserId(user.getId())
                .map(p -> p.getCustomerId())
                .orElse(null);
        String employeeCode = employeeProfileRepository.findByUserId(user.getId())
                .map(p -> p.getEmployeeCode())
                .orElse(null);
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .fullName(user.getFullName())
                .enabled(user.isEnabled())
                .accountLocked(user.isAccountLocked())
                .userType(user.getUserType() != null ? user.getUserType().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .customerId(customerId)
                .employeeCode(employeeCode)
                .roles(rolesToNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
