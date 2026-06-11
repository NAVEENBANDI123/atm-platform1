package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.request.LoanApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.LoanAccountResponse;
import com.atm.dto.response.LoanApplicationResponse;
import com.atm.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@Tag(name = "Loans", description = "Customer loan application + employee review/approval")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;

    @Operation(summary = "Customer: apply for a loan")
    @PostMapping("/applications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> apply(
            Authentication auth, @Valid @RequestBody LoanApplyRequest request) {
        LoanApplicationResponse resp = service.apply(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resp, "Loan application submitted"));
    }

    @Operation(summary = "Customer: list my loan applications")
    @GetMapping("/applications/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> myApplications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.myApplications(auth.getName())));
    }

    @Operation(summary = "Customer: list my disbursed loans + repayment schedule")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<LoanAccountResponse>>> myLoans(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.myLoans(auth.getName())));
    }

    @Operation(summary = "Officer/Admin: list pending loan applications")
    @GetMapping("/applications/pending")
    public ResponseEntity<ApiResponse<PageResponse<LoanApplicationResponse>>> listPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(service.listPending(pageable)));
    }

    @Operation(summary = "LOAN_OFFICER: review and recommend or return")
    @PostMapping("/applications/{id}/review")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> review(
            @PathVariable Long id, @Valid @RequestBody ReviewRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.review(id, request, auth.getName()), "Review recorded"));
    }

    @Operation(summary = "SUPER_ADMIN: approve a reviewed loan application")
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ApiResponse<LoanAccountResponse>> approve(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.approve(id, auth.getName()), "Loan approved and disbursed"));
    }

    @Operation(summary = "SUPER_ADMIN: reject a loan application with a reason")
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody RejectionRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.reject(id, request, auth.getName()), "Loan application rejected"));
    }
}
