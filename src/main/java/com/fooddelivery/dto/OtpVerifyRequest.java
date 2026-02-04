package com.fooddelivery.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {

    // Email OTP verification (ACTIVE)
    private String email;

    /* ---------------- PHONE OTP DISABLED ----------------
       phoneNumber is no longer used for OTP verification.
       It's kept only as optional unused information.
    ------------------------------------------------------- */
    private String phoneNumber;

    private String otp;

    // "user" or "admin" – still used for email OTP flow
    private String type;
}
