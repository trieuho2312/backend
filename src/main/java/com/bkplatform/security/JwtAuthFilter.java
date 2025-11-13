package com.bkplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // ✅ Skip OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Log request for debugging (optional, remove in production)
        String path = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), path);

        // Extract Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // ✅ Skip if no token present (public endpoints)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No JWT token found in request");
            chain.doFilter(request, response);
            return;
        }

        try {
            // Extract token
            String token = authHeader.substring(BEARER_PREFIX.length());

            // Extract username from token
            String username = jwtUtil.extractUsername(token);

            log.debug("JWT token found for user: {}", username);

            // ✅ Authenticate if not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // ✅ Validate token
                if (jwtUtil.isTokenValid(token, userDetails)) {

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("User {} authenticated successfully", username);

                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                }
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            // ✅ Optional: Set custom header to inform client
            response.setHeader("X-Token-Expired", "true");

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());

        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());

        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());

        } catch (IllegalArgumentException e) {
            log.error("JWT token is invalid: {}", e.getMessage());

        } catch (Exception e) {
            // ✅ FIX: Use proper logger instead of System.err
            log.error("JWT validation error: {}", e.getMessage(), e);
        }

        // ✅ Always continue the filter chain
        chain.doFilter(request, response);
    }

    /**
     * Skip JWT filter for specific paths (optional)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip filter for public paths
        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/error");
    }
}