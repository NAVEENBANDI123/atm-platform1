package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.TransferRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.BalanceResponse;
import com.atm.dto.response.CustomerDashboardResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.service.AccountService;
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

@Tag(name = "Customer Account", description = "Customer account view, masked dashboard, balance reveal, transfer")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Get my account (balance is intentionally null - masked)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> myAccount(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getMyAccount(auth.getName())));
    }

    @Operation(summary = "Get the customer dashboard payload (no balance)")
    @GetMapping("/me/dashboard")
    public ResponseEntity<ApiResponse<CustomerDashboardResponse>> myDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getMyDashboard(auth.getName())));
    }

    @Operation(summary = "Reveal my balance (called when 'Show Balance' is clicked)")
    @GetMapping("/me/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> myBalance(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getMyBalance(auth.getName())));
    }

    @Operation(summary = "Transfer funds to a beneficiary or another account number")
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            Authentication auth, @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                accountService.transfer(auth.getName(), request), "Transfer successful"));
    }
}
