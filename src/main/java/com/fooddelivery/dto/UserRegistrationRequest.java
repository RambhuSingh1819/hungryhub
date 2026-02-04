package com.fooddelivery.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    private String email;

    /* ---------------- PHONE VERIFICATION DISABLED ----------------
       Phone number is optional and NOT used for OTP or login anymore.
       Keeping it only as stored profile information.
    ---------------------------------------------------------------- */
    private String phoneNumber;

    private String password;
    private String fullName;
    private String address;
}
