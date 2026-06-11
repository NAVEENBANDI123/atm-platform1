package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Transactions", description = "Customer transaction history, mini statement and download")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Paged transaction history for the authenticated customer")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> history(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getHistory(auth.getName(), pageable)));
    }

    @Operation(summary = "Last 5 transactions (mini statement)")
    @GetMapping("/mini-statement")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> miniStatement(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getMiniStatement(auth.getName())));
    }

    @Operation(summary = "Download statement as CSV")
    @GetMapping(value = "/statement", produces = "text/csv")
    public ResponseEntity<String> downloadStatement(Authentication auth) {
        String csv = transactionService.exportStatementCsv(auth.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"statement.csv\"")
                .body(csv);
    }
}
