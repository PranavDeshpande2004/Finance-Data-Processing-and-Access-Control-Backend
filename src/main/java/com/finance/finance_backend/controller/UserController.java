package com.finance.finance_backend.controller;

import com.finance.finance_backend.dto.UserRequest;
import com.finance.finance_backend.dto.ApiResponse;
import com.finance.finance_backend.dto.UserResponse;
import com.finance.finance_backend.ratelimit.RateLimitInterceptor;
import com.finance.finance_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user management operations.
 * All endpoints restricted to ADMIN role only.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User Management", description = "Admin-only user management endpoints")
public class UserController {

    private final UserService           userService;
    private final RateLimitInterceptor  rateLimitInterceptor;

    /**
     * Retrieves all users in the system.
     *
     * @param request the HTTP request (used for rate limiting)
     * @return list of all users
     */
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }

    /**
     * Retrieves a single user by ID.
     *
     * @param id      the user ID
     * @param request the HTTP request (used for rate limiting)
     * @return user details
     */
    @Operation(summary = "Get user by ID", description = "Returns a single user by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("GET /api/users/{}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", user)
        );
    }

    /**
     * Updates an existing user's name, role, and active status.
     *
     * @param id      the user ID
     * @param request the update request body
     * @return updated user details
     */
    @Operation(summary = "Update user", description = "Updates name, role, and active status")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitInterceptor.checkRateLimit(httpRequest);
        log.info("PUT /api/users/{}", id);
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", updated)
        );
    }

    /**
     * Toggles a user's active/inactive status.
     *
     * @param id      the user ID
     * @param request the HTTP request (used for rate limiting)
     * @return updated user details
     */
    @Operation(
            summary     = "Toggle user status",
            description = "Activates or deactivates a user account"
    )
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        rateLimitInterceptor.checkRateLimit(request);
        log.info("PATCH /api/users/{}/toggle-status", id);
        UserResponse updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", updated)
        );
    }
}
