package com.fooddelivery.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fooddelivery.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username}}")
    private String from;

    @Override
    @Async("taskExecutor")
    public void sendOtpEmail(String to, String subject, String otpCode, String purposeText) {
        try {
            String body = "Your OTP for " + purposeText + " is: " + otpCode +
                    "\n\nThis OTP is valid for 10 minutes." +
                    "\nIf you did not request this, please ignore this email.";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Successfully sent OTP email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
        }
    }
}
