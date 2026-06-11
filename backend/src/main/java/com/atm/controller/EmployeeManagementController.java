package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.request.EmployeeCreateRequest;
import com.atm.dto.request.EmployeeUpdateRequest;
import com.atm.dto.response.EmployeeResponse;
import com.atm.service.EmployeeManagementService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Employee Management", description = "Create / edit / disable employee accounts (SUPER_ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class EmployeeManagementController {

    private final EmployeeManagementService service;

    @Operation(summary = "List all employees")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("id").ascending());
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @Operation(summary = "Get a single employee")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @Operation(summary = "Create a new employee account and assign a role")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeCreateRequest request, Authentication authentication) {
        EmployeeResponse resp = service.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resp, "Employee created"));
    }

    @Operation(summary = "Edit employee details / change role")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.update(id, request, authentication.getName()), "Employee updated"));
    }

    @Operation(summary = "Disable an employee account")
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<EmployeeResponse>> disable(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.disable(id, authentication.getName()), "Employee disabled"));
    }

    @Operation(summary = "Re-enable a previously disabled employee account")
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<EmployeeResponse>> enable(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                service.enable(id, authentication.getName()), "Employee enabled"));
    }
}
