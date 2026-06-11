package com.atm.mapper;

import com.atm.dto.response.AuditLogResponse;
import com.atm.dto.response.BeneficiaryResponse;
import com.atm.dto.response.CardApplicationResponse;
import com.atm.dto.response.CardResponse;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.dto.response.DepositProductResponse;
import com.atm.dto.response.EmployeeResponse;
import com.atm.dto.response.LoanApplicationResponse;
import com.atm.dto.response.LoanScheduleEntryResponse;
import com.atm.dto.response.NomineeResponse;
import com.atm.dto.response.NotificationResponse;
import com.atm.dto.response.TicketResponse;
import com.atm.entity.AuditLog;
import com.atm.entity.Beneficiary;
import com.atm.entity.Card;
import com.atm.entity.CardApplication;
import com.atm.entity.Complaint;
import com.atm.entity.CustomerProfile;
import com.atm.entity.DepositProduct;
import com.atm.entity.EmployeeProfile;
import com.atm.entity.LoanApplication;
import com.atm.entity.LoanScheduleEntry;
import com.atm.entity.Nominee;
import com.atm.entity.Notification;
import com.atm.entity.Role;
import com.atm.entity.User;
import com.atm.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hand-written central mapping between persistence entities and the
 * response DTOs.  Some mappings (e.g. customer profile) require an
 * extra lookup for the linked account; those go through an injected
 * {@link AccountRepository} so we can resolve the account number
 * without forcing a JPA association on every read path.
 */
@Component
@RequiredArgsConstructor
public class DomainMapper {

    private final AccountRepository accountRepository;

    // ---------- Customer profile ----------

    public CustomerProfileResponse toCustomerProfileResponse(CustomerProfile p) {
        if (p == null) return null;
        User u = p.getUser();
        String accountNumber = (u == null) ? null
                : accountRepository.findByUserId(u.getId())
                        .map(a -> a.getAccountNumber()).orElse(null);
        return CustomerProfileResponse.builder()
                .id(p.getId())
                .userId(u == null ? null : u.getId())
                .customerId(p.getCustomerId())
                .username(u == null ? null : u.getUsername())
                .email(u == null ? null : u.getEmail())
                .mobile(u == null ? null : u.getMobile())
                .prefix(p.getPrefix())
                .firstName(p.getFirstName())
                .middleName(p.getMiddleName())
                .lastName(p.getLastName())
                .fullName(u == null ? null : u.getFullName())
                .gender(p.getGender())
                .dateOfBirth(p.getDateOfBirth())
                .aadhaar(p.getAadhaar())
                .pan(p.getPan())
                .houseNumber(p.getHouseNumber())
                .street(p.getStreet())
                .area(p.getArea())
                .city(p.getCity())
                .state(p.getState())
                .country(p.getCountry())
                .postalCode(p.getPostalCode())
                .kycStatus(p.getKycStatus() == null ? null : p.getKycStatus().name())
                .customerStatus(p.getCustomerStatus() == null ? null : p.getCustomerStatus().name())
                .requestedAccountType(p.getRequestedAccountType() == null ? null
                        : p.getRequestedAccountType().name())
                .rejectionReason(p.getRejectionReason())
                .submittedAt(p.getSubmittedAt())
                .approvedAt(p.getApprovedAt())
                .accountNumber(accountNumber)
                .build();
    }

    // ---------- Employee ----------

    public EmployeeResponse toEmployeeResponse(User user, EmployeeProfile profile) {
        if (user == null) return null;
        return EmployeeResponse.builder()
                .id(user.getId())
                .employeeCode(profile == null ? null : profile.getEmployeeCode())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .fullName(user.getFullName())
                .designation(profile == null ? null : profile.getDesignation())
                .department(profile == null ? null : profile.getDepartment())
                .status(user.getStatus() == null ? null : user.getStatus().name())
                .enabled(user.isEnabled())
                .roles(roleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ---------- Cards ----------

    public CardApplicationResponse toCardApplicationResponse(CardApplication a) {
        if (a == null) return null;
        return CardApplicationResponse.builder()
                .id(a.getId())
                .applicantUsername(a.getUser() == null ? null : a.getUser().getUsername())
                .applicantName(a.getUser() == null ? null : a.getUser().getFullName())
                .accountNumber(a.getAccount() == null ? null : a.getAccount().getAccountNumber())
                .cardType(a.getCardType() == null ? null : a.getCardType().name())
                .status(a.getStatus() == null ? null : a.getStatus().name())
                .reviewNote(a.getReviewNote())
                .rejectReason(a.getRejectReason())
                .createdAt(a.getCreatedAt())
                .approvedAt(a.getApprovedAt())
                .rejectedAt(a.getRejectedAt())
                .build();
    }

    public CardResponse toCardResponse(Card c) {
        if (c == null) return null;
        return CardResponse.builder()
                .id(c.getId())
                .maskedNumber(c.getMaskedNumber())
                .cardType(c.getCardType() == null ? null : c.getCardType().name())
                .status(c.getStatus() == null ? null : c.getStatus().name())
                .expiryDate(c.getExpiryDate())
                .dailyLimit(c.getDailyLimit())
                .accountNumber(c.getAccount() == null ? null : c.getAccount().getAccountNumber())
                .build();
    }

    // ---------- Loans ----------

    public LoanApplicationResponse toLoanApplicationResponse(LoanApplication a) {
        if (a == null) return null;
        return LoanApplicationResponse.builder()
                .id(a.getId())
                .applicantUsername(a.getUser() == null ? null : a.getUser().getUsername())
                .applicantName(a.getUser() == null ? null : a.getUser().getFullName())
                .loanType(a.getLoanType() == null ? null : a.getLoanType().name())
                .amount(a.getAmount())
                .tenureMonths(a.getTenureMonths())
                .monthlyIncome(a.getMonthlyIncome())
                .employmentType(a.getEmploymentType())
                .employerName(a.getEmployerName())
                .purpose(a.getPurpose())
                .status(a.getStatus() == null ? null : a.getStatus().name())
                .reviewNote(a.getReviewNote())
                .rejectReason(a.getRejectReason())
                .createdAt(a.getCreatedAt())
                .approvedAt(a.getApprovedAt())
                .rejectedAt(a.getRejectedAt())
                .build();
    }

    public LoanScheduleEntryResponse toLoanScheduleEntry(LoanScheduleEntry e) {
        if (e == null) return null;
        return LoanScheduleEntryResponse.builder()
                .installmentNo(e.getInstallmentNo())
                .dueDate(e.getDueDate())
                .emiAmount(e.getEmiAmount())
                .principalPart(e.getPrincipalPart())
                .interestPart(e.getInterestPart())
                .balance(e.getBalance())
                .paid(e.isPaid())
                .build();
    }

    // ---------- Beneficiary / Nominee ----------

    public BeneficiaryResponse toBeneficiaryResponse(Beneficiary b) {
        if (b == null) return null;
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .nickname(b.getNickname())
                .accountNumber(b.getAccountNumber())
                .beneficiaryName(b.getBeneficiaryName())
                .bankName(b.getBankName())
                .ifsc(b.getIfsc())
                .verified(b.isVerified())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public NomineeResponse toNomineeResponse(Nominee n) {
        if (n == null) return null;
        return NomineeResponse.builder()
                .id(n.getId())
                .accountNumber(n.getAccount() == null ? null : n.getAccount().getAccountNumber())
                .name(n.getName())
                .relationship(n.getRelationship())
                .dateOfBirth(n.getDateOfBirth())
                .sharePercent(n.getSharePercent())
                .build();
    }

    // ---------- Deposits / Tickets / Notifications / Audit ----------

    public DepositProductResponse toDepositProductResponse(DepositProduct d) {
        if (d == null) return null;
        return DepositProductResponse.builder()
                .id(d.getId())
                .depositType(d.getDepositType() == null ? null : d.getDepositType().name())
                .principal(d.getPrincipal())
                .interestRate(d.getInterestRate())
                .tenureMonths(d.getTenureMonths())
                .monthlyAmount(d.getMonthlyAmount())
                .maturityDate(d.getMaturityDate())
                .maturityAmount(d.getMaturityAmount())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .build();
    }

    public TicketResponse toTicketResponse(Complaint c) {
        if (c == null) return null;
        return TicketResponse.builder()
                .id(c.getId())
                .customerUsername(c.getUser() == null ? null : c.getUser().getUsername())
                .customerFullName(c.getUser() == null ? null : c.getUser().getFullName())
                .subject(c.getSubject())
                .description(c.getDescription())
                .status(c.getStatus() == null ? null : c.getStatus().name())
                .resolution(c.getResolution())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    public NotificationResponse toNotificationResponse(Notification n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .body(n.getBody())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    public AuditLogResponse toAuditLogResponse(AuditLog a) {
        if (a == null) return null;
        return AuditLogResponse.builder()
                .id(a.getId())
                .username(a.getUsername())
                .userRole(a.getUserRole())
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .details(a.getDetails())
                .oldValue(a.getOldValue())
                .newValue(a.getNewValue())
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private Set<String> roleNames(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}
