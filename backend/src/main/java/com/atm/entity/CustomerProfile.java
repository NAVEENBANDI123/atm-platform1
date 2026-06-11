package com.atm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * KYC + onboarding payload for a {@link UserType#CUSTOMER}.
 * One-to-one with {@link User}.  The {@link #customerStatus} drives the
 * approval workflow described in {@link CustomerStatus}.
 */
@Entity
@Table(name = "customer_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "customer_id", length = 20, unique = true)
    private String customerId;

    @Column(length = 5)
    private String prefix;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "middle_name", length = 60)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 12, unique = true)
    private String aadhaar;

    @Column(nullable = false, length = 10, unique = true)
    private String pan;

    @Column(name = "house_number", length = 40)
    private String houseNumber;

    @Column(length = 120)
    private String street;

    @Column(length = 120)
    private String area;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String country;

    @Column(name = "postal_code", length = 12)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus customerStatus = CustomerStatus.PENDING_APPROVAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_account_type", nullable = false, length = 20)
    @Builder.Default
    private AccountType requestedAccountType = AccountType.SAVINGS;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
