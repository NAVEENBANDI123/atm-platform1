package com.atm.controller;

import com.atm.common.ApiResponse;
import com.atm.dto.response.UserResponse;
import com.atm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "User profile")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the authenticated user's profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> profile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(authentication.getName())));
    }
}
