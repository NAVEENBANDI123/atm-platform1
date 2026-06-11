package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.common.PageResponse;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.entity.Account;
import com.atm.entity.AccountStatus;
import com.atm.entity.CustomerProfile;
import com.atm.entity.CustomerStatus;
import com.atm.entity.KycStatus;
import com.atm.entity.User;
import com.atm.entity.UserStatus;
import com.atm.exception.BadRequestException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.CustomerProfileRepository;
import com.atm.repository.UserRepository;
import com.atm.service.CustomerOnboardingService;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import com.atm.util.IdentifierGenerators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerOnboardingServiceImpl implements CustomerOnboardingService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final CustomerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final IdentifierGenerators identifiers;
    private final EmailService emailService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final DomainMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerProfileResponse> listByStatus(CustomerStatus status, Pageable pageable) {
        return PageResponse.from(profileRepository.findByCustomerStatus(status, pageable)
                .map(mapper::toCustomerProfileResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getById(Long profileId) {
        CustomerProfile p = profileRepository.findById(profileId)
                .orElseThrow(() -> ResourceNotFoundException.of("CustomerProfile", "id", profileId));
        return mapper.toCustomerProfileResponse(p);
    }

    @Override
    @Transactional
    public CustomerProfileResponse approve(Long profileId, String approverUsername) {
        CustomerProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> ResourceNotFoundException.of("CustomerProfile", "id", profileId));
        if (profile.getCustomerStatus() != CustomerStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Customer is not awaiting approval (current state: "
                    + profile.getCustomerStatus() + ")");
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));
        User user = profile.getUser();

        // 1. Issue a customer ID + open the account
        if (profile.getCustomerId() == null) {
            profile.setCustomerId(identifiers.nextCustomerId());
        }
        String accountNumber;
        int retry = 0;
        do {
            accountNumber = generateAccountNumber();
            retry++;
            if (retry > 10) throw new IllegalStateException("Could not allocate account number");
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .status(AccountStatus.ACTIVE)
                .accountType(profile.getRequestedAccountType())
                .openedAt(LocalDateTime.now())
                .build();
        accountRepository.save(account);

        // 2. Activate the user + mark profile approved
        profile.setCustomerStatus(CustomerStatus.APPROVED);
        profile.setKycStatus(KycStatus.VERIFIED);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(approver.getId());
        profile.setRejectionReason(null);
        profileRepository.save(profile);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // 3. Audit
        auditService.recordWithValues(
                "APPROVE_CUSTOMER", "CUSTOMER_PROFILE", String.valueOf(profile.getId()),
                "Approved customer " + user.getUsername(),
                "PENDING_APPROVAL", "APPROVED");

        // 4. Email + in-app notification
        emailService.send(user.getEmail(),
                "Your account is approved",
                "customer-approved",
                Map.of(
                        "name", user.getFullName(),
                        "customerId", profile.getCustomerId(),
                        "accountNumber", accountNumber,
                        "accountType", profile.getRequestedAccountType().name()));
        notificationService.push(user, "Account approved",
                "Welcome aboard! Your customer ID is " + profile.getCustomerId()
                        + " and your account number is " + accountNumber + ".");

        return mapper.toCustomerProfileResponse(profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse reject(Long profileId, RejectionRequest request, String approverUsername) {
        CustomerProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> ResourceNotFoundException.of("CustomerProfile", "id", profileId));
        if (profile.getCustomerStatus() == CustomerStatus.APPROVED) {
            throw new BadRequestException("Cannot reject an already approved customer");
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        String previous = profile.getCustomerStatus().name();
        profile.setCustomerStatus(CustomerStatus.REJECTED);
        profile.setRejectionReason(request.reason());
        profile.setApprovedBy(approver.getId());
        profile.setApprovedAt(LocalDateTime.now());
        profileRepository.save(profile);

        User user = profile.getUser();
        user.setStatus(UserStatus.REJECTED);
        userRepository.save(user);

        auditService.recordWithValues(
                "REJECT_CUSTOMER", "CUSTOMER_PROFILE", String.valueOf(profile.getId()),
                "Rejected customer " + user.getUsername() + ": " + request.reason(),
                previous, "REJECTED");

        emailService.send(user.getEmail(),
                "Your account application was not approved",
                "customer-rejected",
                Map.of("name", user.getFullName(), "reason", request.reason()));

        return mapper.toCustomerProfileResponse(profile);
    }

    private static String generateAccountNumber() {
        StringBuilder sb = new StringBuilder(12);
        sb.append(1 + RANDOM.nextInt(9));
        for (int i = 0; i < 11; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
