package com.finance.finance_backend.controller;

import com.finance.finance_backend.dto.LoginRequest;
import com.finance.finance_backend.dto.RegisterRequest;
import com.finance.finance_backend.dto.ApiResponse;
import com.finance.finance_backend.dto.AuthResponse;
import com.finance.finance_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 * Public endpoints — no JWT token required.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request body
     * @return JWT token and user info
     */
    @Operation(
            summary     = "Register a new user",
            description = "Creates a new user account and returns a JWT token"
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("POST /api/auth/register — email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request the login request body
     * @return JWT token and user info
     */
    @Operation(
            summary     = "Login",
            description = "Authenticates user credentials and returns a JWT token"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/auth/login — email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }
}
