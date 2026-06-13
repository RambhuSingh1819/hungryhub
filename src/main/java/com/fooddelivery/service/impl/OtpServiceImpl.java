package com.fooddelivery.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.OtpVerification.OtpType;
import com.fooddelivery.repository.OtpVerificationRepository;
import com.fooddelivery.service.EmailService;
import com.fooddelivery.service.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration:600000}")
    private long otpExpirationMs;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Override
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    @Async("taskExecutor")
    public void sendOtpToEmail(String email, String otp, OtpType type) {
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
            case RESET_PASSWORD, ADMIN_RESET_PASSWORD -> {
                subject = "Password Reset OTP";
                purpose = "resetting your password";
            }
            default -> {
                subject = "Your One Time Password (OTP)";
                purpose = "verification";
            }
        }

        emailService.sendOtpEmail(email, subject, otp, purpose);
        log.info("Delegated OTP email dispatch to EmailService for {} with type {}", maskEmail(email), type);
    }

    @Override
    @Transactional
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

        if (type == OtpType.EMAIL || type == OtpType.ADMIN_EMAIL || type == OtpType.RESET_PASSWORD || type == OtpType.ADMIN_RESET_PASSWORD) {
            sendOtpToEmail(identifier, otp, type);
        } else {
            log.info("OTP send skipped for identifier {} with type {} (phone OTP disabled)", identifier, type);
        }

        return otpVerification;
    }

    @Override
    @Transactional
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

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
