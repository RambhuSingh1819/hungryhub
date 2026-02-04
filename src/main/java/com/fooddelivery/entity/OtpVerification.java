package com.fooddelivery.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * identifier = email (phone OTP disabled)
     */
    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;
    /*
        Allowed Types Now:
        ✔ EMAIL
        ✔ ADMIN_EMAIL
        ✔ RESET_PASSWORD

        Disabled (commented):
        ❌ PHONE
        ❌ ADMIN_PHONE
    */

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(5);
        }
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public enum OtpType {

        EMAIL,          // user email verification

        /* -------------------- PHONE OTP DISABLED --------------------
        PHONE,          // user phone verification
        ---------------------------------------------------------------- */

        ADMIN_EMAIL,    // admin email verification

        /* -------------------- ADMIN PHONE OTP DISABLED --------------------
        ADMIN_PHONE,    // admin phone verification
        ------------------------------------------------------------------- */

        RESET_PASSWORD  // forgot password flow
, ADMIN_RESET_PASSWORD
    }
}
