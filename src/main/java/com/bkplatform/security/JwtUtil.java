package com.bkplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    // ✅ Minimum key size for HS256 is 256 bits (32 bytes)
    private static final int MIN_KEY_LENGTH = 32;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        // ✅ FIX: Use UTF-8 encoding explicitly
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // ✅ Validate key length
        if (keyBytes.length < MIN_KEY_LENGTH) {
            log.error("JWT secret key is too short. Minimum length is {} bytes", MIN_KEY_LENGTH);
            throw new IllegalArgumentException(
                    String.format("JWT secret must be at least %d characters long", MIN_KEY_LENGTH)
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;

        log.info("JwtUtil initialized with expiration: {}ms", expiration);
    }

    /**
     * Generate JWT token with claims
     */
    public String generateToken(String username, Map<String, Object> claims) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            log.debug("Generated token for user: {}", username);
            return token;

        } catch (Exception e) {
            log.error("Error generating token for user: {}", username, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validate token against user details
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

            if (!isValid) {
                log.warn("Token validation failed for user: {}", username);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Get remaining validity time in milliseconds
     */
    public long getRemainingValidity(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Validate token structure without checking expiration
     */
    public boolean validateTokenStructure(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token structure is valid but expired
            return true;
        } catch (Exception e) {
            log.error("Token structure validation failed: {}", e.getMessage());
            return false;
        }
    }
}