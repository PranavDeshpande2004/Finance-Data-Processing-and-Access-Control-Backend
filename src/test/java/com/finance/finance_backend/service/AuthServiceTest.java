package com.finance.finance_backend.service;

import static org.junit.jupiter.api.Assertions.*;



import com.finance.finance_backend.dto.LoginRequest;
import com.finance.finance_backend.dto.RegisterRequest;
import com.finance.finance_backend.dto.AuthResponse;
import com.finance.finance_backend.entity.User;
import com.finance.finance_backend.enums.Role;
import com.finance.finance_backend.exception.DuplicateResourceException;
import com.finance.finance_backend.repository.UserRepository;
import com.finance.finance_backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests registration, login, and security constraints.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtUtil               jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest    loginRequest;
    private User            mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Viewer");
        registerRequest.setEmail("viewer@finance.com");
        registerRequest.setPassword("viewer123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@finance.com");
        loginRequest.setPassword("admin123");

        mockUser = User.builder()
                .id(1L)
                .name("John Viewer")
                .email("viewer@finance.com")
                .password("encodedPassword")
                .role(Role.VIEWER)
                .isActive(true)
                .build();
    }

    // ─────────────────────────────────────────
    // REGISTER TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Register — success — auto assigns VIEWER role")
    void register_success_autoAssignsViewerRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);
        assertThat(response.getEmail()).isEqualTo("viewer@finance.com");
        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        // Verify role is ALWAYS VIEWER regardless of input
        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.VIEWER
        ));
    }

    @Test
    @DisplayName("Register — fails — duplicate email throws DuplicateResourceException")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("viewer@finance.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("viewer@finance.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register — password is BCrypt encoded before saving")
    void register_passwordIsEncodedBeforeSaving() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("viewer123")).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        authService.register(registerRequest);

        verify(passwordEncoder).encode("viewer123");
        verify(userRepository).save(argThat(user ->
                !user.getPassword().equals("viewer123")
        ));
    }

    @Test
    @DisplayName("Register — new user is active by default")
    void register_newUser_isActiveByDefault() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(user ->
                Boolean.TRUE.equals(user.getIsActive())
        ));
    }

    // ─────────────────────────────────────────
    // LOGIN TESTS
    // ─────────────────────────────────────────

    @Test
    @DisplayName("Login — success — returns token and user info")
    void login_success_returnsTokenAndUserInfo() {
        User adminUser = User.builder()
                .id(1L)
                .name("Super Admin")
                .email("admin@finance.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(userRepository.findByEmail("admin@finance.com"))
                .thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken("admin@finance.com"))
                .thenReturn("admin.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getToken()).isEqualTo("admin.jwt.token");
        assertThat(response.getEmail()).isEqualTo("admin@finance.com");
    }

    @Test
    @DisplayName("Login — fails — bad credentials throws exception")
    void login_badCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login — calls authenticationManager with correct credentials")
    void login_callsAuthManagerWithCorrectCredentials() {
        User adminUser = User.builder()
                .email("admin@finance.com")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(adminUser));
        when(jwtUtil.generateToken(any())).thenReturn("token");

        authService.login(loginRequest);

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth instanceof UsernamePasswordAuthenticationToken &&
                                auth.getPrincipal().equals("admin@finance.com")
                )
        );
    }
}