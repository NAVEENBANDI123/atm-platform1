package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.BeneficiaryCreateRequest;
import com.atm.dto.response.BeneficiaryResponse;
import com.atm.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Beneficiaries", description = "Customer beneficiary management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class BeneficiaryController {

    private final BeneficiaryService service;

    @Operation(summary = "Add a new beneficiary")
    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> add(
            @Valid @RequestBody BeneficiaryCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.add(auth.getName(), request), "Beneficiary added"));
    }

    @Operation(summary = "List my beneficiaries")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.list(auth.getName())));
    }

    @Operation(summary = "Remove a beneficiary")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long id, Authentication auth) {
        service.remove(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Beneficiary removed"));
    }
}
