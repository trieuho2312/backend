package com.bkplatform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.frontend-url}")
    private String frontendUrl;

    /**
     * ✅ Gửi email đơn giản (text thuần)
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * ✅ Gửi email HTML (đẹp hơn)
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

            log.info("✅ HTML Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * 📧 Gửi email chào mừng sau khi đăng ký
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        String subject = "🎉 Welcome to BK Platform!";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background: #f9f9f9; border-radius: 5px; margin-top: 20px; }
                    .button { display: inline-block; padding: 12px 24px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to BK Platform!</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s! 👋</h2>
                        <p>Thank you for registering with BK Platform.</p>
                        <p>Your account has been successfully created. You can now:</p>
                        <ul>
                            <li>✅ Browse and purchase products</li>
                            <li>✅ Create your own shop</li>
                            <li>✅ Sell your products</li>
                            <li>✅ Manage orders and inventory</li>
                        </ul>
                        <a href="%s" class="button">Start Shopping</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 BK Platform. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, frontendUrl);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 🔐 Gửi email reset password
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "🔑 Password Reset Request";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f44336; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background: #f9f9f9; border-radius: 5px; margin-top: 20px; }
                    .button { display: inline-block; padding: 12px 24px; background: #f44336; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
                    .warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <h2>Reset Your Password</h2>
                        <p>We received a request to reset your password for your BK Platform account.</p>
                        <p>Click the button below to reset your password:</p>
                        <a href="%s" class="button">Reset Password</a>
                        <div class="warning">
                            <strong>⚠️ Security Note:</strong>
                            <ul style="margin: 10px 0 0 0;">
                                <li>This link will expire in 1 hour</li>
                                <li>If you didn't request this, please ignore this email</li>
                                <li>Never share this link with anyone</li>
                            </ul>
                        </div>
                        <p style="margin-top: 20px; font-size: 12px; color: #666;">
                            Or copy this link: <br>
                            <code style="background: #e0e0e0; padding: 5px; display: inline-block; margin-top: 5px;">%s</code>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2024 BK Platform. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetLink, resetLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 📦 Gửi email xác nhận đơn hàng
     */
    @Async
    public void sendOrderConfirmationEmail(String to, Long orderId, String totalAmount) {
        String subject = "✅ Order Confirmation #" + orderId;

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { padding: 20px; background: #f9f9f9; border-radius: 5px; margin-top: 20px; }
                    .order-box { background: white; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .button { display: inline-block; padding: 12px 24px; background: #2196F3; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Order Confirmed!</h1>
                    </div>
                    <div class="content">
                        <h2>Thank you for your order!</h2>
                        <p>Your order has been successfully placed and is being processed.</p>
                        <div class="order-box">
                            <strong>Order Details:</strong>
                            <p style="margin: 10px 0 5px 0;">
                                📋 Order ID: <strong>#%d</strong><br>
                                💰 Total: <strong>%s VND</strong><br>
                                📅 Date: <strong>%s</strong>
                            </p>
                        </div>
                        <a href="%s/orders/%d" class="button">View Order Details</a>
                    </div>
                    <div class="footer">
                        <p>© 2024 BK Platform. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, orderId, totalAmount, java.time.LocalDateTime.now().toString(), frontendUrl, orderId);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 🔔 Test gửi email
     */
    public boolean testEmailConnection(String testEmail) {
        try {
            sendSimpleEmail(
                    testEmail,
                    "✅ BK Platform Email Test",
                    "Congratulations! Email configuration is working correctly.\n\n" +
                            "You can now receive:\n" +
                            "- Welcome emails\n" +
                            "- Password reset emails\n" +
                            "- Order confirmations\n\n" +
                            "Best regards,\nBK Platform Team"
            );
            return true;
        } catch (Exception e) {
            log.error("Email test failed", e);
            return false;
        }
    }
}
