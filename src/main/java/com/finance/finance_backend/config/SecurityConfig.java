package com.finance.finance_backend.config;

import com.finance.finance_backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 * Defines JWT-based stateless authentication,
 * CORS policy, and role-based access control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter      jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * Main security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // ── Disable CSRF — stateless JWT API ──
                .csrf(AbstractHttpConfigurer::disable)

                // ── Enable CORS ──
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── Stateless session — no HttpSession ──
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── URL authorization rules ──
                .authorizeHttpRequests(auth -> auth

                        // Public — no token needed
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        // Dashboard — all 3 roles
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**")
                        .hasAnyRole("VIEWER", "ANALYST", "ADMIN")

                        // Transactions read — Analyst + Admin
                        .requestMatchers(HttpMethod.GET, "/api/transactions/**")
                        .hasAnyRole("ANALYST", "ADMIN")

                        // Transactions write — Admin only
                        .requestMatchers(HttpMethod.POST, "/api/transactions/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/transactions/**")
                        .hasRole("ADMIN")

                        // User management — Admin only
                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // ── Auth provider ──
                .authenticationProvider(authenticationProvider())

                // ── JWT filter before Spring's default auth filter ──
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS configuration — allows React frontend
     * running on localhost:3000 to call the API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins — add your frontend URL here
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allowed headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Apply CORS config to all endpoints
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCrypt password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DAO authentication provider with
     * custom UserDetailsService and BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes AuthenticationManager for AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}