package com.fooddelivery.service;

import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.OtpVerification.OtpType;

public interface OtpService {
    String generateOtp();
    void sendOtpToEmail(String email, String otp, OtpType type);
    OtpVerification createAndSendOtp(String identifier, OtpType type);
    boolean verifyOtp(String identifier, String otp, OtpType type);
}
