package com.bkplatform.service;

import com.bkplatform.dto.RegisterRequest;
import com.bkplatform.exception.InvalidDataException;
import com.bkplatform.model.Role;
import com.bkplatform.model.User;
import com.bkplatform.repository.UserRepository;
import com.bkplatform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // ✅ Stronger email validation pattern
    private static final Pattern HUST_EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@hust\\.edu\\.vn$",
            Pattern.CASE_INSENSITIVE
    );

    // ✅ Password strength requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"
    );

    /**
     * Validate HUST email with stronger pattern
     */
    public boolean isHustEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return HUST_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password strength
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidDataException(
                    String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH)
            );
        }

        // ✅ Optional: Enforce password complexity (uncomment if needed)
        // if (!PASSWORD_PATTERN.matcher(password).matches()) {
        //     throw new InvalidDataException(
        //         "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
        //     );
        // }
    }

    /**
     * Register new user
     * ✅ FIX: Send welcome email after successful registration
     */
    @Transactional
    public User register(RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());

        // ✅ Validate HUST email
        if (!isHustEmail(req.getEmail())) {
            throw new InvalidDataException("Email must belong to @hust.edu.vn domain");
        }

        // ✅ Validate password
        validatePassword(req.getPassword());

        // ✅ Check username uniqueness
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new InvalidDataException("Username already exists");
        }

        // ✅ Check email uniqueness
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new InvalidDataException("Email already registered");
        }

        // ✅ Normalize email (lowercase)
        String normalizedEmail = req.getEmail().toLowerCase().trim();

        // Create user
        User user = User.builder()
                .username(req.getUsername().trim())
                .fullName(req.getFullName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", saved.getUsername());

        // ✅ FIX: Send welcome email asynchronously
        try {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());
            log.info("Welcome email sent to: {}", saved.getEmail());
        } catch (Exception e) {
            // Don't fail registration if email fails
            log.error("Failed to send welcome email to: {}", saved.getEmail(), e);
        }

        return saved;
    }

    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        log.debug("Generating token for user: {}", user.getUsername());

        var claims = new HashMap<String, Object>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());

        return jwtUtil.generateToken(user.getUsername(), claims);
    }

    /**
     * Validate user credentials
     */
    public User authenticate(String usernameOrEmail, String password) {
        // Find user by username or email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElse(null));

        // ✅ Generic error message to prevent user enumeration
        if (user == null) {
            log.warn("Authentication failed: user not found - {}", usernameOrEmail);
            throw new InvalidDataException("Invalid username/email or password");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: incorrect password for user {}", user.getUsername());
            throw new InvalidDataException("Invalid username/email or password");
        }

        log.info("User authenticated successfully: {}", user.getUsername());
        return user;
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", user.getUsername());

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidDataException("Current password is incorrect");
        }

        // Validate new password
        validatePassword(newPassword);

        // Check new password is different
        if (oldPassword.equals(newPassword)) {
            throw new InvalidDataException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Request password reset
     * ✅ FIX: Actually send reset email
     */
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        // Find user by email
        User user = userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);

        // ✅ Don't reveal if email exists (security best practice)
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Still return success to prevent email enumeration
            return;
        }

        // ✅ Generate reset token (simple implementation - use UUID in production)
        String resetToken = generateResetToken(user);

        // ✅ Send password reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            // Don't expose error to user
        }
    }

    /**
     * Generate reset token (simple implementation)
     * ✅ TODO: In production, use proper token generation and storage
     */
    private String generateResetToken(User user) {
        // Simple implementation - you should use UUID and store in database
        // with expiration time in production
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Reset password with token
     * ✅ TODO: Implement token validation logic
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // TODO: Validate token from database
        // TODO: Check token expiration
        // TODO: Get user from token
        // TODO: Update password

        throw new UnsupportedOperationException("Reset password with token not yet implemented");
    }
}