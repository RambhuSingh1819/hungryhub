package com.fooddelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Admin login ID
    Optional<Admin> findByAdminId(String adminId);

    // Email-based admin lookup (still required for email OTP)
    Optional<Admin> findByEmail(String email);

    /* ---------------------- PHONE LOOKUPS DISABLED ----------------------

    // Admin phone-based login or verification (disabled)
    Optional<Admin> findByPhoneNumber(String phoneNumber);

    // Phone number uniqueness for admin (disabled)
    boolean existsByPhoneNumber(String phoneNumber);

    ----------------------------------------------------------------------- */

    boolean existsByEmail(String email);

    boolean existsByAdminId(String adminId);
}
