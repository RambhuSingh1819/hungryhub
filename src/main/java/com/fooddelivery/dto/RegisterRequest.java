package com.fooddelivery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String fullName;
    private String email;

    /* ---------------- PHONE OTP DISABLED ----------------
       Phone number is optional and NO OTP is required.
       It will be stored as plain text only.
    ------------------------------------------------------- */
    private String phoneNumber;

    private String password;
    private String confirmPassword;
    private String address;

    // Email OTP still required
    private String emailOtp;

    /* ---------------- PHONE OTP DISABLED ----------------
       private String phoneOtp;
    ------------------------------------------------------- */
}
