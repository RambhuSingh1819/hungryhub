package com.fooddelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.OtpVerification.OtpType;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    /**
     * Find latest OTP for given identifier (email) and type.
     * Phone types are disabled at enum level (no PHONE / ADMIN_PHONE usage).
     */
    Optional<OtpVerification> findTopByIdentifierAndTypeOrderByCreatedAtDesc(
            String identifier,
            OtpType type
    );
}
