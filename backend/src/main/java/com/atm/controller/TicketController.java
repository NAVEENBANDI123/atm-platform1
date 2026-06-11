package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.request.TicketCreateRequest;
import com.atm.dto.request.TicketResolveRequest;
import com.atm.dto.response.TicketResponse;
import com.atm.entity.TicketStatus;
import com.atm.service.TicketService;
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

@Tag(name = "Tickets", description = "Customer support tickets / complaints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService service;

    @Operation(summary = "Customer: open a new support ticket")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            @Valid @RequestBody TicketCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(auth.getName(), request), "Ticket created"));
    }

    @Operation(summary = "Customer: list my tickets")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> mine(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.mine(auth.getName())));
    }

    @Operation(summary = "Employee: list tickets by status")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ACCOUNTANT','CASHIER','CARD_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> list(
            @RequestParam(defaultValue = "OPEN") TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(service.listByStatus(status, pageable)));
    }

    @Operation(summary = "Employee: resolve / progress / close a ticket")
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ACCOUNTANT','CASHIER','CARD_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<ApiResponse<TicketResponse>> resolve(
            @PathVariable Long id, @Valid @RequestBody TicketResolveRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.resolve(id, request, auth.getName()), "Ticket updated"));
    }
}
