package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.response.AuditLogResponse;
import com.atm.service.AuditQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit", description = "SUPER_ADMIN read-only audit trail")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AuditController {

    private final AuditQueryService service;

    @Operation(summary = "List audit log entries (latest first)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by("createdAt").descending());
        if (username != null && !username.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(service.listByUsername(username, pageable)));
        }
        if (entityType != null && !entityType.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(service.listByEntityType(entityType, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }
}
