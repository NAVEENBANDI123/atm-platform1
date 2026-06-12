package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.config.AppProperties;
import com.atm.dto.request.CustomerRegisterRequest;
import com.atm.dto.request.ForgotPasswordRequest;
import com.atm.dto.request.LoginRequest;
import com.atm.dto.request.RefreshTokenRequest;
import com.atm.dto.request.ResetPasswordRequest;
import com.atm.dto.response.AuthResponse;
import com.atm.dto.response.CustomerRegistrationResponse;
import com.atm.entity.CustomerProfile;
import com.atm.entity.CustomerStatus;
import com.atm.entity.KycStatus;
import com.atm.entity.RefreshToken;
import com.atm.entity.Role;
import com.atm.entity.RoleName;
import com.atm.entity.User;
import com.atm.entity.UserStatus;
import com.atm.entity.UserType;
import com.atm.exception.BadRequestException;
import com.atm.exception.DuplicateResourceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.UserMapper;
import com.atm.repository.CustomerProfileRepository;
import com.atm.repository.RefreshTokenRepository;
import com.atm.repository.RoleRepository;
import com.atm.repository.UserRepository;
import com.atm.security.JwtTokenProvider;
import com.atm.service.AuthService;
import com.atm.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final AuditService auditService;
    private final UserMapper userMapper;
    private final EmailService emailService;

    // ---------- Customer registration ----------

    @Override
    @Transactional
    public CustomerRegistrationResponse registerCustomer(CustomerRegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByMobile(req.mobile())) {
            throw new DuplicateResourceException("Mobile already registered");
        }
        if (customerProfileRepository.existsByAadhaar(req.aadhaar())) {
            throw new DuplicateResourceException("Aadhaar already registered");
        }
        if (customerProfileRepository.existsByPan(req.pan())) {
            throw new DuplicateResourceException("PAN already registered");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER missing; check seed data"));

        String fullName = buildFullName(req.prefix(), req.firstName(), req.middleName(), req.lastName());

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .mobile(req.mobile())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(fullName)
                .enabled(true)
                .accountLocked(false)
                .userType(UserType.CUSTOMER)
                .status(UserStatus.PENDING_APPROVAL)
                .roles(Set.of(customerRole))
                .build();
        user = userRepository.save(user);

        CustomerProfile profile = CustomerProfile.builder()
                .user(user)
                .prefix(req.prefix())
                .firstName(req.firstName())
                .middleName(req.middleName())
                .lastName(req.lastName())
                .gender(req.gender())
                .dateOfBirth(req.dateOfBirth())
                .aadhaar(req.aadhaar())
                .pan(req.pan())
                .houseNumber(req.houseNumber())
                .street(req.street())
                .area(req.area())
                .city(req.city())
                .state(req.state())
                .country(req.country())
                .postalCode(req.postalCode())
                .kycStatus(KycStatus.PENDING)
                .customerStatus(CustomerStatus.PENDING_APPROVAL)
                .requestedAccountType(req.accountType())
                .submittedAt(LocalDateTime.now())
                .build();
        customerProfileRepository.save(profile);

        auditService.record("CUSTOMER_REGISTER", "USER", String.valueOf(user.getId()),
                "Customer self-registered: " + user.getUsername());

        // ----- Notify customer & admins (asynchronous) -----
        Map<String, Object> customerVars = new HashMap<>();
        customerVars.put("name", fullName);
        customerVars.put("username", user.getUsername());
        emailService.send(
                user.getEmail(),
                "Your account application has been received",
                "customer-registration-submitted",
                customerVars);

        Map<String, Object> adminVars = new HashMap<>();
        adminVars.put("name", fullName);
        adminVars.put("username", user.getUsername());
        adminVars.put("email", user.getEmail());
        adminVars.put("mobile", user.getMobile());
        for (String admin : appProperties.getMail().getAdminRecipients()) {
            emailService.send(admin,
                    "New customer awaiting approval: " + user.getUsername(),
                    "admin-customer-pending", adminVars);
        }

        return CustomerRegistrationResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .message("Your account application has been submitted successfully and is waiting for approval.")
                .build();
    }

    // ---------- Logins ----------

    @Override
    @Transactional
    public AuthResponse customerLogin(LoginRequest request) {
        User user = authenticate(request);
        if (user.getUserType() != UserType.CUSTOMER) {
            throw new BadRequestException("Please use the employee portal to sign in.");
        }
        switch (user.getStatus()) {
            case PENDING_APPROVAL ->
                    throw new BadRequestException(
                            "Your account is awaiting approval. You will receive an email once it is approved.");
            case REJECTED ->
                    throw new BadRequestException(
                            "Your registration was rejected. Please contact support for assistance.");
            case SUSPENDED ->
                    throw new BadRequestException("Your account is suspended. Please contact support.");
            case DISABLED ->
                    throw new BadRequestException("Your account is disabled.");
            case ACTIVE -> { /* allow */ }
        }
        auditService.record("LOGIN_SUCCESS", "USER", String.valueOf(user.getId()),
                "Customer login: " + user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse employeeLogin(LoginRequest request) {
        User user = authenticate(request);
        if (user.getUserType() != UserType.EMPLOYEE) {
            throw new BadRequestException("Please use the customer portal to sign in.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Your employee account is " + user.getStatus().name().toLowerCase()
                    + ". Please contact your administrator.");
        }
        auditService.record("LOGIN_SUCCESS", "USER", String.valueOf(user.getId()),
                "Employee login: " + user.getUsername());
        return buildAuthResponse(user);
    }

    // ---------- Refresh / logout ----------

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (stored.isRevoked() || stored.isExpired()) {
            throw new BadRequestException("Refresh token expired or revoked");
        }
        User user = stored.getUser();
        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), roleNames(user));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(stored.getToken())
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getAccessTokenExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            auditService.record("LOGOUT", "USER", String.valueOf(token.getUser().getId()), null);
        });
    }

    // ---------- Forgot / reset password ----------

    @Override
    @Transactional(readOnly = true)
    public String customerForgotPassword(ForgotPasswordRequest request) {
        return issueResetToken(request, UserType.CUSTOMER, "Customer password reset request");
    }

    @Override
    @Transactional(readOnly = true)
    public String employeeForgotPassword(ForgotPasswordRequest request) {
        return issueResetToken(request, UserType.EMPLOYEE, "Employee password reset request");
    }

    private String issueResetToken(ForgotPasswordRequest request, UserType expectedType,
                                   String emailSubject) {
        User user = userRepository.findByUsernameAndMobile(request.username(), request.mobile())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account matches the provided username and mobile"));
        if (user.getUserType() != expectedType) {
            throw new BadRequestException(expectedType == UserType.CUSTOMER
                    ? "This username is not a customer account. Use the employee portal instead."
                    : "This username is not an employee account. Use the customer portal instead.");
        }
        String resetToken = tokenProvider.generateAccessToken(user.getUsername(), List.of("PWD_RESET"));
        auditService.record("FORGOT_PASSWORD", "USER", String.valueOf(user.getId()),
                expectedType.name() + " requested password reset");

        emailService.send(user.getEmail(), emailSubject, "customer-password-reset",
                Map.of("name", user.getFullName(), "resetToken", resetToken));
        return resetToken;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!tokenProvider.isValid(request.resetToken())) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        String username = tokenProvider.getUsername(request.resetToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        refreshTokenRepository.revokeAllForUser(user);
        auditService.record("RESET_PASSWORD", "USER", String.valueOf(user.getId()), null);

        emailService.send(user.getEmail(),
                "Your password has been changed",
                "customer-password-changed",
                Map.of("name", user.getFullName()));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (isLocked(user)) {
            throw new BadRequestException(
                    "Account is locked due to too many failed attempts. Try again later.");
        }
        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new BadRequestException("Invalid username or password");
        }
        // success -> reset counters
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
        return user;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), roleNames(user));
        RefreshToken refreshToken = createRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getAccessTokenExpirationMs())
                .user(userMapper.toResponse(user))
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(
                        appProperties.getJwt().getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).toList();
    }

    private boolean isLocked(User user) {
        if (!user.isAccountLocked()) {
            return false;
        }
        LocalDateTime lockTime = user.getLockTime();
        if (lockTime == null) {
            return true;
        }
        LocalDateTime unlockAt = lockTime.plusMinutes(appProperties.getSecurity().getLockDurationMinutes());
        if (LocalDateTime.now().isAfter(unlockAt)) {
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return false;
        }
        return true;
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= appProperties.getSecurity().getMaxFailedAttempts()) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            auditService.record("ACCOUNT_LOCKED", "USER", String.valueOf(user.getId()),
                    "Locked after " + attempts + " failed attempts");
        }
        userRepository.save(user);
    }

    private static String buildFullName(String prefix, String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isBlank()) sb.append(prefix).append(' ');
        sb.append(first.trim());
        if (middle != null && !middle.isBlank()) sb.append(' ').append(middle.trim());
        sb.append(' ').append(last.trim());
        return sb.toString();
    }
}
