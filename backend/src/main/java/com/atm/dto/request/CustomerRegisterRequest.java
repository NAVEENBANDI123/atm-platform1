package com.atm.dto.request;

import com.atm.entity.AccountType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Full customer registration payload used by the customer portal.
 *
 * <p>Captures personal information, identity (Aadhaar/PAN), address,
 * the requested account product and the desired credentials. The newly
 * created user lands in {@code PENDING_APPROVAL} until an
 * ACCOUNTANT or SUPER_ADMIN approves it.</p>
 */
public record CustomerRegisterRequest(
        // ---- Personal ----
        @NotBlank @Pattern(regexp = "^(Mr|Mrs|Ms|Dr)$",
                message = "Prefix must be one of Mr, Mrs, Ms, Dr") String prefix,
        @NotBlank @Size(min = 1, max = 60) String firstName,
        @Size(max = 60) String middleName,
        @NotBlank @Size(min = 1, max = 60) String lastName,
        @NotBlank @Pattern(regexp = "^(Male|Female|Other)$",
                message = "Gender must be Male, Female or Other") String gender,
        @NotNull @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$",
                message = "Mobile must be 10 digits") String mobile,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{12}$",
                message = "Aadhaar must be 12 digits") String aadhaar,
        @NotBlank @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$",
                message = "PAN must look like AAAAA9999A") String pan,

        // ---- Address ----
        @NotBlank @Size(max = 40) String houseNumber,
        @NotBlank @Size(max = 120) String street,
        @NotBlank @Size(max = 120) String area,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 80) String state,
        @NotBlank @Size(max = 80) String country,
        @NotBlank @Pattern(regexp = "^[0-9]{4,12}$",
                message = "Postal code must be 4-12 digits") String postalCode,

        // ---- Product ----
        @NotNull AccountType accountType,

        // ---- Security ----
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_.]+$",
                message = "Username may contain letters, digits, '.' and '_'") String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(min = 8, max = 72) String confirmPassword
) {
    @AssertTrue(message = "Password and confirmation do not match")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
