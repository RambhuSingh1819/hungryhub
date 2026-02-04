package com.fooddelivery.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.OtpVerification.OtpType;
import com.fooddelivery.repository.OtpVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final JavaMailSender mailSender;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration:600000}")
    private long otpExpirationMs;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${spring.mail.from:${spring.mail.username}}")
    private String fromEmail;

    // Generate numeric OTP
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    // ==============================
    // EMAIL OTP — ACTIVE
    // ==============================
    @Async
    public void sendOtpToEmail(String email, String otp, OtpType type) {
        try {
            String subject;
            String purpose;

            switch (type) {
                case EMAIL -> {
                    subject = "Email Verification OTP";
                    purpose = "verifying your email during registration";
                }
                case ADMIN_EMAIL -> {
                    subject = "Admin Email Verification OTP";
                    purpose = "verifying your admin email";
                }
                case RESET_PASSWORD -> {
                    subject = "Password Reset OTP";
                    purpose = "resetting your password";
                }
                default -> {
                    subject = "Your One Time Password (OTP)";
                    purpose = "verification";
                }
            }

            String body = "Dear user,\n\n" +
                    "Your OTP for " + purpose + " is: " + otp + "\n\n" +
                    "This OTP is valid for " + (otpExpirationMs / 60000) + " minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Regards,\nFood Delivery Team";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("OTP email sent to {} for type {}", maskEmail(email), type);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
        }
    }

    // ============================================================
    // PHONE OTP — FULLY DISABLED (COMMENTED, NOT DELETED)
    // ============================================================

    /*
    @Async
    public void sendOtpToPhone(String phoneNumber, String otp, OtpType type) {
        // PHONE OTP DISABLED — NO SMS WILL BE SENT
        log.info("PHONE OTP DISABLED. Skipped sending SMS to {} for type {}", maskPhone(phoneNumber), type);
    }
    */

    // Create + send OTP
    public OtpVerification createAndSendOtp(String identifier, OtpType type) {
        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(otpExpirationMs / 1000);

        OtpVerification otpVerification = OtpVerification.builder()
                .identifier(identifier)
                .otp(otp)
                .type(type)
                .createdAt(now)
                .expiresAt(expiresAt)
                .verified(false)
                .build();

        otpVerification = otpRepository.save(otpVerification);

        // ONLY EMAIL-BASED TYPES ARE ACTIVE NOW
        if (type == OtpType.EMAIL || type == OtpType.ADMIN_EMAIL || type == OtpType.RESET_PASSWORD) {
            sendOtpToEmail(identifier, otp, type);
        } else {
            // PHONE / OTHER TYPES DISABLED
            log.info("OTP send skipped for identifier {} with type {} (phone OTP disabled)", identifier, type);
            // sendOtpToPhone(identifier, otp, type); // <- remains disabled
        }

        return otpVerification;
    }

    // Verify OTP (works for email + reset password)
    public boolean verifyOtp(String identifier, String otp, OtpType type) {
        Optional<OtpVerification> otpVerificationOpt =
                otpRepository.findTopByIdentifierAndTypeOrderByCreatedAtDesc(identifier, type);

        if (otpVerificationOpt.isEmpty()) {
            return false;
        }

        OtpVerification record = otpVerificationOpt.get();

        if (record.isVerified() || record.isExpired() || !record.getOtp().equals(otp)) {
            return false;
        }

        record.setVerified(true);
        otpRepository.save(record);
        return true;
    }

    // Helpers to mask identifiers in logs
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /* ----------------- PHONE MASKING HELPER DISABLED -----------------

    private String maskPhone(String phone) {
        if (phone.length() <= 4) {
            return "***";
        }
        return "***" + phone.substring(phone.length() - 3);
    }

    ------------------------------------------------------------------ */
}
