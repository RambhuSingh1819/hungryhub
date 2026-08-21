package com.fooddelivery.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fooddelivery.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:foodapp.sender.otp@gmail.com}")
    private String mailUsername;

    @Value("${spring.mail.from:${spring.mail.username:foodapp.sender.otp@gmail.com}}")
    private String mailFrom;

    @Override
    @Async("taskExecutor")
    public void sendOtpEmail(String to, String subject, String otpCode, String purposeText) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String fromAddress = mailUsername;
            if (mailFrom != null && mailFrom.contains("<") && mailFrom.contains(">")) {
                fromAddress = mailFrom.substring(mailFrom.indexOf("<") + 1, mailFrom.indexOf(">")).trim();
            } else if (mailFrom != null && !mailFrom.isBlank()) {
                fromAddress = mailFrom.trim();
            }

            helper.setFrom(fromAddress, "HungryHub");
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlBody = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                        <h2 style="color: #ff4757; text-align: center;">HungryHub OTP Verification</h2>
                        <p style="font-size: 16px; color: #333;">Hello,</p>
                        <p style="font-size: 16px; color: #333;">Your One-Time Password (OTP) for <strong>%s</strong> is:</p>
                        <div style="text-align: center; margin: 25px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #ff4757; background: #fff0f1; padding: 10px 20px; border-radius: 8px; border: 1px dashed #ff4757;">%s</span>
                        </div>
                        <p style="font-size: 14px; color: #666;">This OTP is valid for <strong>10 minutes</strong>. Please do not share this OTP with anyone.</p>
                        <hr style="border: none; border-top: 1px solid #eeeeee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">If you did not request this OTP, please ignore this email.</p>
                    </div>
                    """.formatted(purposeText, otpCode);

            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("Successfully sent OTP email to {}", to);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to send OTP email to {}. Error: {}", to, e.getMessage(), e);
        }
    }
}

