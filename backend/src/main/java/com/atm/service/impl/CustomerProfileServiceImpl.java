package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.dto.request.ChangePasswordRequest;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.entity.CustomerProfile;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.CustomerProfileRepository;
import com.atm.repository.RefreshTokenRepository;
import com.atm.repository.UserRepository;
import com.atm.service.CustomerProfileService;
import com.atm.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private final CustomerProfileRepository repository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final EmailService emailService;
    private final DomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getMyProfile(String username) {
        CustomerProfile p = repository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        return mapper.toCustomerProfileResponse(p);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user);

        auditService.record("CHANGE_PASSWORD", "USER", String.valueOf(user.getId()),
                "User changed their password");
        emailService.send(user.getEmail(),
                "Your password has been changed",
                "customer-password-changed",
                Map.of("name", user.getFullName()));
    }
}
