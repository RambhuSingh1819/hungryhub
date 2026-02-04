package com.fooddelivery.dto;

import lombok.Data;

@Data
public class OtpRequest {

    // Email used for sending OTP (ACTIVE)
    private String email;

    /* ---------------- PHONE OTP DISABLED ----------------
       phoneNumber is NO LONGER used for sending OTP.
       It remains only as optional information (ignored).
    ------------------------------------------------------- */
    private String phoneNumber;

    // "user" or "admin" — still valid for determining EMAIL OTP behavior
    private String type;
}
