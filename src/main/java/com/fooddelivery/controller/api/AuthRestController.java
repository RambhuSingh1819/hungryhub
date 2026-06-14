package com.fooddelivery.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.dto.LoginRequest;
import com.fooddelivery.dto.OtpRequest;
import com.fooddelivery.dto.OtpVerifyRequest;
import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.OtpService;
import com.fooddelivery.service.UserService;
import com.fooddelivery.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody OtpRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (userService.existsByEmail(request.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already registered");
                return ResponseEntity.badRequest().body(response);
            }
            otpService.createAndSendOtp(request.getEmail(), OtpVerification.OtpType.EMAIL);
            response.put("success", true);
            response.put("message", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            boolean emailVerified = otpService.verifyOtp(
                    request.getEmail(),
                    request.getOtp(),
                    OtpVerification.OtpType.EMAIL
            );
            if (emailVerified) {
                response.put("success", true);
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error verifying OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserRegistrationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.registerUser(request);
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            User user = userService.findByEmail(request.getEmail()).orElse(null);
            if (user == null || !userService.validatePassword(request.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                return ResponseEntity.badRequest().body(response);
            }

            String token = jwtTokenProvider.generateToken(user.getEmail(), "USER");
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "address", user.getAddress() != null ? user.getAddress() : ""
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
