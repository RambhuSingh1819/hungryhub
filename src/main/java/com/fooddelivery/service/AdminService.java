package com.fooddelivery.service;

import java.time.LocalDate;
import java.util.Optional;
import com.fooddelivery.dto.AdminRegistrationRequest;
import com.fooddelivery.entity.Admin;

public interface AdminService {
    Optional<Admin> findByAdminId(String adminId);
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByAdminId(String adminId);
    boolean isSubscriptionActive(Admin admin);
    void markAdminSubscriptionPaid(Long adminId, String planType, LocalDate expiry);
    Admin registerAdmin(AdminRegistrationRequest request);
    boolean validatePassword(String rawPassword, String encodedPassword);
    void updatePassword(Admin admin, String newPassword);
    void resetPassword(String email, String newPassword);
}
