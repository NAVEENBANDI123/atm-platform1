package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CustomerProfileResponse(
        Long id,
        Long userId,
        String customerId,
        String username,
        String email,
        String mobile,
        String prefix,
        String firstName,
        String middleName,
        String lastName,
        String fullName,
        String gender,
        LocalDate dateOfBirth,
        String aadhaar,
        String pan,
        String houseNumber,
        String street,
        String area,
        String city,
        String state,
        String country,
        String postalCode,
        String kycStatus,
        String customerStatus,
        String requestedAccountType,
        String rejectionReason,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt,
        String accountNumber
) {
}
