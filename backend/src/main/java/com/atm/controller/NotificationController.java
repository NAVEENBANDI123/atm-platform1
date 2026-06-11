package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.common.PageResponse;
import com.atm.dto.response.NotificationResponse;
import com.atm.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Notifications", description = "In-app notification center")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @Operation(summary = "List my notifications (most recent first)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> list(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(service.list(auth.getName(), pageable)));
    }

    @Operation(summary = "Get my unread notification count")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unread(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("count", service.unreadCount(auth.getName()))));
    }

    @Operation(summary = "Mark all my notifications as read")
    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markRead(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("updated", service.markAllRead(auth.getName())), "Marked as read"));
    }
}
