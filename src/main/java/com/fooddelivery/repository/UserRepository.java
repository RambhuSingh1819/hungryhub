package com.fooddelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fooddelivery.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Email login + email OTP lookups
    Optional<User> findByEmail(String email);

    /* ---------------------- PHONE LOOKUPS DISABLED ----------------------

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    // For login or OTP using phone — now disabled
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    ---------------------------------------------------------------------- */

    // Only email checks are valid now
    boolean existsByEmail(String email);
}
