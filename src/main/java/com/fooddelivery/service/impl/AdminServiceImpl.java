package com.fooddelivery.service.impl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.dto.AdminRegistrationRequest;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.repository.AdminRepository;
import com.fooddelivery.service.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<Admin> findByAdminId(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByAdminId(String adminId) {
        return adminRepository.existsByAdminId(adminId);
    }

    @Override
    public boolean isSubscriptionActive(Admin admin) {
        return admin != null
                && admin.isPaid()
                && admin.getSubscriptionExpiry() != null
                && !admin.getSubscriptionExpiry().isBefore(LocalDate.now());
    }

    @Override
    @Transactional
    public void markAdminSubscriptionPaid(Long adminId, String planType, LocalDate expiry) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        admin.setPaid(true);
        admin.setPlanType(planType);
        admin.setSubscriptionExpiry(expiry);

        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public Admin registerAdmin(AdminRegistrationRequest request) {
        Admin admin = Admin.builder()
                .adminId(generateAdminId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .emailVerified(true)
                .phoneVerified(false)
                .active(true)
                .build();

        return adminRepository.save(admin);
    }

    private String generateAdminId() {
        String adminId;
        do {
            adminId = "ADM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (adminRepository.existsByAdminId(adminId));
        return adminId;
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    @Transactional
    public void updatePassword(Admin admin, String newPassword) {
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
}
