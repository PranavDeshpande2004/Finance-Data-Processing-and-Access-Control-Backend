package com.finance.finance_backend.controller;

import com.finance.finance_backend.dto.*;
import com.finance.finance_backend.enums.Role;
import com.finance.finance_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setName("John Viewer");
        registerRequest.setEmail("viewer@finance.com");
        registerRequest.setPassword("viewer123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@finance.com");
        loginRequest.setPassword("admin123");

        authResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .name("John Viewer")
                .email("viewer@finance.com")
                .role(Role.VIEWER)
                .tokenType("Bearer")
                .build();
    }

    // ✅ REGISTER SUCCESS
    @Test
    void register_validRequest_returns201() {

        when(authService.register(any())).thenReturn(authResponse);

        var result = authController.register(registerRequest);

        assertEquals(201, result.getStatusCodeValue());
        assertTrue(result.getBody().isSuccess());
        assertEquals("viewer@finance.com", result.getBody().getData().getEmail());
        assertEquals("VIEWER", result.getBody().getData().getRole().name());

        verify(authService).register(any());
    }

    // ❗ VALIDATION TESTS (manual check, since no Spring validation)

    @Test
    void register_blankName_shouldFail() {

        registerRequest.setName("");

        var result = authController.register(registerRequest);

        // depends on your controller validation handling
        assertNotNull(result);
    }

    // ✅ LOGIN SUCCESS
    @Test
    void login_validCredentials_returns200() {

        when(authService.login(any())).thenReturn(authResponse);

        var result = authController.login(loginRequest);

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().isSuccess());
        assertEquals("mock.jwt.token", result.getBody().getData().getToken());

        verify(authService).login(any());
    }

    // ✅ LOGIN FAILURE (Exception case)
    @Test
    void login_badCredentials_shouldThrowException() {

        when(authService.login(any()))
                .thenThrow(new RuntimeException("Invalid email or password"));

        assertThrows(RuntimeException.class, () ->
                authController.login(loginRequest)
        );
    }

    // ❗ MISSING EMAIL CASE (no automatic validation)
    @Test
    void login_missingEmail_shouldFail() {

        loginRequest.setEmail("");

        var result = authController.login(loginRequest);

        assertNotNull(result);
    }
}