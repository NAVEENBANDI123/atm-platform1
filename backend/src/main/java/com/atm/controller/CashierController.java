package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.CashierDepositRequest;
import com.atm.dto.request.CashierWithdrawRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.service.CashierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cashier", description = "Cash counter operations - SUPER_ADMIN or CASHIER only")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cashier")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CASHIER')")
public class CashierController {

    private final CashierService service;

    @Operation(summary = "Look up a customer account by number")
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> lookup(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(service.lookup(accountNumber)));
    }

    @Operation(summary = "Deposit cash into a customer account")
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody CashierDepositRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.deposit(request, auth.getName()), "Deposit posted"));
    }

    @Operation(summary = "Withdraw cash from a customer account")
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody CashierWithdrawRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.withdraw(request, auth.getName()), "Withdrawal posted"));
    }
}
