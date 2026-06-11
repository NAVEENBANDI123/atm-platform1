package com.atm.dto.request;

import com.atm.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateRequest(
        @Size(max = 120) String fullName,
        @Email @Size(max = 120) String email,
        @Pattern(regexp = "^[0-9]{10}$") String mobile,
        RoleName role,
        @Size(max = 60) String designation,
        @Size(max = 60) String department
) {
}
