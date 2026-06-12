package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.ForgotPasswordRequest;
import com.atm.dto.request.LoginRequest;
import com.atm.dto.request.RefreshTokenRequest;
import com.atm.dto.request.ResetPasswordRequest;
import com.atm.dto.response.AuthResponse;
import com.atm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Employee-only authentication portal.  Employees never self-register;
 * SUPER_ADMIN provisions employee accounts via {@code /api/v1/admin/employees}.
 *
 * <p>Forgot/Reset password is supported here so an employee who has been
 * locked out can recover access without going through the customer portal.</p>
 */
@Tag(name = "Employee Auth", description = "Employee login, forgot/reset password and token refresh")
@RestController
@RequestMapping("/api/v1/auth/employee")
@RequiredArgsConstructor
public class EmployeeAuthController {

    private final AuthService authService;

    @Operation(summary = "Employee login (any banking role)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.employeeLogin(request), "Login successful"));
    }

    @Operation(summary = "Verify identity (username + mobile) to obtain a password reset token (employee)")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.employeeForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("resetToken", token),
                "Identity verified. A reset token has been emailed to you."));
    }

    @Operation(summary = "Set a new password using a valid employee reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully"));
    }

    @Operation(summary = "Exchange a refresh token for a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request), "Token refreshed"));
    }

    @Operation(summary = "Revoke a refresh token (logout)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out"));
    }
}
