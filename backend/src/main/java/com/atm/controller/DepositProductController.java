package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.FixedDepositRequest;
import com.atm.dto.request.RecurringDepositRequest;
import com.atm.dto.response.DepositProductResponse;
import com.atm.service.DepositProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Deposits", description = "Fixed and Recurring deposit products (customer)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class DepositProductController {

    private final DepositProductService service;

    @Operation(summary = "Open a fixed deposit (debits the principal from your savings)")
    @PostMapping("/fd")
    public ResponseEntity<ApiResponse<DepositProductResponse>> openFd(
            @Valid @RequestBody FixedDepositRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.openFd(auth.getName(), request), "Fixed deposit opened"));
    }

    @Operation(summary = "Open a recurring deposit")
    @PostMapping("/rd")
    public ResponseEntity<ApiResponse<DepositProductResponse>> openRd(
            @Valid @RequestBody RecurringDepositRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.openRd(auth.getName(), request), "Recurring deposit opened"));
    }

    @Operation(summary = "List my deposits (FD + RD)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DepositProductResponse>>> mine(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.myDeposits(auth.getName())));
    }
}
