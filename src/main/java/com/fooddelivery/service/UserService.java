package com.fooddelivery.service;

import java.util.Optional;

import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.User;

public interface UserService {
    Optional<User> findByEmail(String email);
    boolean emailExists(String email);
    boolean existsByEmail(String email);
    User registerUser(UserRegistrationRequest request);
    boolean validatePassword(String rawPassword, String encodedPassword);
    void updatePassword(User user, String newRawPassword);
    void resetPassword(String email, String newPassword);
}
