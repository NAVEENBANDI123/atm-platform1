package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.ChangePasswordRequest;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.service.CustomerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customer Profile", description = "Customer self-service profile and password")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/customer/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {

    private final CustomerProfileService service;

    @Operation(summary = "Get my full customer profile")
    @GetMapping
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> me(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getMyProfile(auth.getName())));
    }

    @Operation(summary = "Change my password")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, Authentication auth) {
        service.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed"));
    }
}
