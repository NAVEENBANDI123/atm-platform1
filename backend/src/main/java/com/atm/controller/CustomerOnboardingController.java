package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.response.CustomerProfileResponse;
import com.atm.entity.CustomerStatus;
import com.atm.service.CustomerOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Customer Onboarding (Admin)",
     description = "List/approve/reject customer registrations - SUPER_ADMIN or ACCOUNTANT only")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ACCOUNTANT')")
public class CustomerOnboardingController {

    private final CustomerOnboardingService service;

    @Operation(summary = "List customer profiles by approval status (default: PENDING_APPROVAL)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerProfileResponse>>> list(
            @RequestParam(defaultValue = "PENDING_APPROVAL") CustomerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("submittedAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(service.listByStatus(status, pageable)));
    }

    @Operation(summary = "Get a single customer profile")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @Operation(summary = "Approve a customer (issues customer ID + account number)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> approve(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.approve(id, authentication.getName()), "Customer approved"));
    }

    @Operation(summary = "Reject a customer with a reason")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody RejectionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.reject(id, request, authentication.getName()), "Customer rejected"));
    }
}
