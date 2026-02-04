package com.fooddelivery.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.CartRepository;
import com.fooddelivery.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    // ============== FIND METHODS ==============

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /* -------------------- PHONE LOOKUP DISABLED --------------------

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<User> findByEmailOrPhone(String identifier) {
        return userRepository.findByEmailOrPhoneNumber(identifier, identifier);
    }

    ----------------------------------------------------------------- */

    // ============== EXISTS METHODS ==============

    /**
     * Wrapper for existsByEmail()
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /* -------------------- PHONE EXISTS DISABLED --------------------

    public boolean phoneExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    ---------------------------------------------------------------- */

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ============== REGISTER USER (AFTER EMAIL OTP VERIFIED) ==============

    @Transactional
    public User registerUser(UserRegistrationRequest request) {

        User user = User.builder()
                .email(request.getEmail())
                // phone number is optional, not verified
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .address(request.getAddress())
                .emailVerified(true)     // email OTP is verified in controller

                /* ---------- PHONE OTP / VERIFICATION DISABLED ----------
                .phoneVerified(true)
                --------------------------------------------------------- */

                .phoneVerified(false)    // never used now

                .enabled(true)
                .build();

        user = userRepository.save(user);

        // Create user cart
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        return user;
    }

    // ============== PASSWORD HELPERS ==============

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void updatePassword(User user, String newRawPassword) {
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }
 // ========================================
 // ✅ RESET PASSWORD USING EMAIL (FOR FORGOT PASSWORD)
 // ========================================
 @Transactional
 public void resetPassword(String email, String newPassword) {

     User user = userRepository.findByEmail(email)
             .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

     user.setPassword(passwordEncoder.encode(newPassword));
     userRepository.save(user);
 }

}
