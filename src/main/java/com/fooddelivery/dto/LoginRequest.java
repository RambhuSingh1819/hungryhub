package com.fooddelivery.dto;

import lombok.Data;

@Data
public class LoginRequest {

    // Email is now the ONLY valid login identifier for users
    private String email;

    /* ---------------- PHONE LOGIN DISABLED ----------------
       phoneNumber will NOT be used for login anymore.
       It is kept only as optional unused profile info.
    --------------------------------------------------------- */
    private String phoneNumber;

    // Admin login still works with adminId + password
    private String adminId;

    private String password;

    // "user" or "admin" — still used in controller
    private String type;
}
