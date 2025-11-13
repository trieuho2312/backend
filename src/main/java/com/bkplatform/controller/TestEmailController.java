package com.bkplatform.controller;

import com.bkplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ✅ Controller để test email
 * CHÚ Ý: Xóa hoặc secure controller này trong production!
 */
@Slf4j
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
public class TestEmailController {

    private final EmailService emailService;

    /**
     * Test gửi email đơn giản
     *
     * Test với Postman/curl:
     * POST http://localhost:8080/api/test/email/simple
     * Body: { "to": "your-email@gmail.com" }
     */
    @PostMapping("/simple")
    public ResponseEntity<?> testSimpleEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");

        if (to == null || to.isEmpty()) {
            return ResponseEntity.badRequest().body("Email address is required");
        }

        try {
            emailService.sendSimpleEmail(
                    to,
                    "✅ Test Email from BK Platform",
                    "Hello!\n\n" +
                            "This is a test email from BK Platform.\n\n" +
                            "If you receive this, your email configuration is working correctly!\n\n" +
                            "Best regards,\n" +
                            "BK Platform Team"
            );

            log.info("Test email sent to: {}", to);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email sent successfully to " + to,
                    "tip", "Check your inbox (and spam folder!)"
            ));

        } catch (Exception e) {
            log.error("Failed to send test email", e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to send email: " + e.getMessage(),
                    "tip", "Check your email configuration in application.properties"
            ));
        }
    }

    /**
     * Test gửi email HTML
     */
    @PostMapping("/welcome")
    public ResponseEntity<?> testWelcomeEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String username = request.getOrDefault("username", "Test User");

        if (to == null || to.isEmpty()) {
            return ResponseEntity.badRequest().body("Email address is required");
        }

        try {
            emailService.sendWelcomeEmail(to, username);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Welcome email sent to " + to
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test gửi password reset email
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> testResetPasswordEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String token = request.getOrDefault("token", "sample-reset-token-12345");

        if (to == null || to.isEmpty()) {
            return ResponseEntity.badRequest().body("Email address is required");
        }

        try {
            emailService.sendPasswordResetEmail(to, token);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Password reset email sent to " + to
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test order confirmation email
     */
    @PostMapping("/order-confirmation")
    public ResponseEntity<?> testOrderConfirmationEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        Long orderId = Long.parseLong(request.getOrDefault("orderId", "12345"));
        String totalAmount = request.getOrDefault("totalAmount", "500000");

        if (to == null || to.isEmpty()) {
            return ResponseEntity.badRequest().body("Email address is required");
        }

        try {
            emailService.sendOrderConfirmationEmail(to, orderId, totalAmount);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Order confirmation email sent to " + to
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test kết nối email
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection(@RequestParam String email) {
        boolean success = emailService.testEmailConnection(email);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email connection is working! Check " + email
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Email connection failed. Check logs for details."
            ));
        }
    }
}