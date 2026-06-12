package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.CustomerRegisterRequest;
import com.atm.dto.request.ForgotPasswordRequest;
import com.atm.dto.request.LoginRequest;
import com.atm.dto.request.RefreshTokenRequest;
import com.atm.dto.request.ResetPasswordRequest;
import com.atm.dto.response.AuthResponse;
import com.atm.dto.response.CustomerRegistrationResponse;
import com.atm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Customer-only authentication portal.  Employees cannot use these
 * endpoints; they are routed to {@link EmployeeAuthController}.
 */
@Tag(name = "Customer Auth", description = "Customer registration, login and password reset")
@RestController
@RequestMapping("/api/v1/auth/customer")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final AuthService authService;

    @Operation(summary = "Submit a new customer registration (PENDING_APPROVAL)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerRegistrationResponse>> register(
            @Valid @RequestBody CustomerRegisterRequest request) {
        CustomerRegistrationResponse resp = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resp, resp.message()));
    }

    @Operation(summary = "Customer login (blocked while PENDING_APPROVAL/REJECTED/SUSPENDED)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.customerLogin(request), "Login successful"));
    }

    @Operation(summary = "Verify identity to obtain a password reset token (customer)")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.customerForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("resetToken", token),
                "Identity verified. A reset token has been emailed to you."));
    }

    @Operation(summary = "Set a new password using a valid reset token")
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
