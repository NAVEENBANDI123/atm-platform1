package com.atm.dto.request;

import com.atm.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_.]+$") String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$") String mobile,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull RoleName role,
        @Size(max = 60) String designation,
        @Size(max = 60) String department
) {
}
