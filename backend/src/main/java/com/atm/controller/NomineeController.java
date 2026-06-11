package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.request.NomineeCreateRequest;
import com.atm.dto.response.NomineeResponse;
import com.atm.service.NomineeService;
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

@Tag(name = "Nominees", description = "Account nominee management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/nominees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class NomineeController {

    private final NomineeService service;

    @Operation(summary = "Add a nominee to my account")
    @PostMapping
    public ResponseEntity<ApiResponse<NomineeResponse>> add(
            @Valid @RequestBody NomineeCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.add(auth.getName(), request), "Nominee added"));
    }

    @Operation(summary = "List nominees on my account")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NomineeResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.list(auth.getName())));
    }

    @Operation(summary = "Remove a nominee")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long id, Authentication auth) {
        service.remove(auth.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Nominee removed"));
    }
}
