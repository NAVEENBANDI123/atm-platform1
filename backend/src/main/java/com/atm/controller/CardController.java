package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.request.CardApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.CardApplicationResponse;
import com.atm.dto.response.CardResponse;
import com.atm.service.CardService;
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

@Tag(name = "Cards", description = "Customer card application + employee review/approval")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService service;

    @Operation(summary = "Customer: apply for a debit/credit card")
    @PostMapping("/applications")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CardApplicationResponse>> apply(
            Authentication auth, @Valid @RequestBody CardApplyRequest request) {
        CardApplicationResponse resp = service.apply(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resp, "Card application submitted"));
    }

    @Operation(summary = "Customer: list my card applications")
    @GetMapping("/applications/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<CardApplicationResponse>>> myApplications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.myApplications(auth.getName())));
    }

    @Operation(summary = "Customer: list my issued cards")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<CardResponse>>> myCards(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.myCards(auth.getName())));
    }

    @Operation(summary = "Officer/Admin: list pending card applications")
    @GetMapping("/applications/pending")
    public ResponseEntity<ApiResponse<PageResponse<CardApplicationResponse>>> listPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(service.listPending(pageable)));
    }

    @Operation(summary = "CARD_OFFICER: review and recommend or return")
    @PostMapping("/applications/{id}/review")
    public ResponseEntity<ApiResponse<CardApplicationResponse>> review(
            @PathVariable Long id, @Valid @RequestBody ReviewRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.review(id, request, auth.getName()), "Review recorded"));
    }

    @Operation(summary = "SUPER_ADMIN: approve a reviewed card application and issue the card")
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ApiResponse<CardResponse>> approve(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.approve(id, auth.getName()), "Card issued"));
    }

    @Operation(summary = "SUPER_ADMIN: reject a card application with a reason")
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<CardApplicationResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody RejectionRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.reject(id, request, auth.getName()), "Card application rejected"));
    }
}
