package com.fooddelivery.dto;

import lombok.Data;

@Data
public class AdminRegistrationRequest {

    private String email;

    /* ------------------- PHONE VERIFICATION DISABLED -------------------
       Phone number is now OPTIONAL and NOT used for OTP or login anymore.
       It will be stored as plain text only.
    --------------------------------------------------------------------- */
    private String phoneNumber;

    private String password;
    private String fullName;

    // No OTP fields required (email OTP handled separately)
}
