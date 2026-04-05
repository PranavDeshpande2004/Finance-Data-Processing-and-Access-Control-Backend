package com.finance.finance_backend.service;

import com.finance.finance_backend.dto.LoginRequest;
import com.finance.finance_backend.dto.RegisterRequest;
import com.finance.finance_backend.dto.AuthResponse;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.Role;
import com.finance.finance_backend.exception.DuplicateResourceException;
import com.finance.finance_backend.repository.UserRepository;
import com.finance.finance_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user registration and login authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     * Validates email uniqueness before saving.
     *
     * @param request the registration request DTO
     * @return AuthResponse containing JWT token and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail()
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .role(request.getRole())
                .role(Role.VIEWER)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully with email: {}", request.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());

        return buildAuthResponse(token, user);
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request the login request DTO
     * @return AuthResponse containing JWT token and user info
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("User logged in successfully: {}", request.getEmail());

        return buildAuthResponse(token, user);
    }

    /**
     * Builds an AuthResponse from a token and user entity.
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
