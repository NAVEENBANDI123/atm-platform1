package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.LoginRequest;
import com.atm.dto.request.RefreshTokenRequest;
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

/**
 * Employee-only authentication portal.  Employees never self-register;
 * SUPER_ADMIN provisions employee accounts via {@code /api/v1/admin/employees}.
 */
@Tag(name = "Employee Auth", description = "Employee login and token refresh")
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
