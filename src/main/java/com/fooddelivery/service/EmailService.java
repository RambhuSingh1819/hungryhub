package com.fooddelivery.service;

public interface EmailService {
    void sendOtpEmail(String to, String subject, String otpCode, String purposeText);
}
