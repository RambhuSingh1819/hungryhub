package com.fooddelivery.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique admin code (ex: ADM001)
    @Column(unique = true, nullable = false)
    private String adminId;

    @Column(unique = true, nullable = false)
    private String email;

    /* ---------------------- PHONE OTP DISABLED ----------------------
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    ------------------------------------------------------------------ */

    // Phone number now OPTIONAL (no validation, no OTP)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private boolean emailVerified = false;

    /* ---------------------- PHONE VERIFIED DISABLED ----------------------
    private boolean phoneVerified = false;
    ------------------------------------------------------------------------ */

    // Always false, not used anywhere in logic
    private boolean phoneVerified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean active = true;

    private boolean paid;                 // kya admin ne pay kiya hai?
    private String planType;              // e.g. "MONTHLY", "YEARLY"
    private LocalDate subscriptionExpiry;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
