package com.finance.finance_backend.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter — runs once per request.
 * Skips public endpoints entirely.
 * Validates JWT and sets Spring Security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String        BEARER_PREFIX = "Bearer ";
    private static final String        AUTH_HEADER   = "Authorization";
    private static final AntPathMatcher PATH_MATCHER  = new AntPathMatcher();

    // These paths skip JWT validation completely
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    private final JwtUtil                jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Skips filter for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (token == null) {
            log.debug("No JWT token found for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authenticated: {} | URI: {}",
                    email, request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts Bearer token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) &&
                header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
