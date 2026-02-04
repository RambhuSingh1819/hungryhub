package com.fooddelivery.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fooddelivery.dto.ForgotPasswordRequest;
import com.fooddelivery.dto.RegisterRequest;
import com.fooddelivery.dto.ResetPasswordRequest;
import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.OtpVerification.OtpType;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.OtpService;
import com.fooddelivery.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserAuthController {

    private final OtpService otpService;
    private final UserService userService;

    // ---------- REGISTER PAGE ----------
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "user/register";
    }

    // ---------- SEND EMAIL OTP ----------
    @PostMapping("/otp/send-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmailOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Map<String, Object> res = new HashMap<>();

        if (email == null || email.isBlank()) {
            res.put("success", false);
            res.put("message", "Email is required");
            return ResponseEntity.badRequest().body(res);
        }

        if (userService.emailExists(email)) {
            res.put("success", false);
            res.put("message", "Email already registered");
            return ResponseEntity.ok(res);
        }

        otpService.createAndSendOtp(email, OtpType.EMAIL);
        res.put("success", true);
        res.put("message", "OTP sent to email");
        return ResponseEntity.ok(res);
    }

    // ---------- SEND PHONE OTP (DISABLED) ----------
    @PostMapping("/otp/send-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendPhoneOtp(@RequestBody Map<String, String> body) {

        /* ------------- PHONE OTP DISABLED ---------------
        String phone = body.get("phone");
        Map<String, Object> res = new HashMap<>();

        if (phone == null || phone.isBlank()) {
            res.put("success", false);
            res.put("message", "Phone number is required");
            return ResponseEntity.badRequest().body(res);
        }

        if (userService.phoneExists(phone)) {
            res.put("success", false);
            res.put("message", "Phone already registered");
            return ResponseEntity.ok(res);
        }

        otpService.createAndSendOtp(phone, OtpType.PHONE);
        res.put("success", true);
        res.put("message", "OTP sent to phone");
        return ResponseEntity.ok(res);
        -------------------------------------------------- */

        Map<String, Object> disabled = new HashMap<>();
        disabled.put("success", false);
        disabled.put("message", "Phone OTP is disabled");
        return ResponseEntity.ok(disabled);
    }

    // ---------- HANDLE REGISTRATION ----------
    @PostMapping("/register")
    public String handleRegister(
            @ModelAttribute("registerRequest") RegisterRequest req,
            Model model
    ) {
        // password match
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "user/register";
        }

        if (userService.emailExists(req.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "user/register";
        }

        // PHONE EXIST CHECK DISABLED
        /*
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && userService.phoneExists(req.getPhoneNumber())) {
            model.addAttribute("error", "Phone already registered");
            return "user/register";
        }
        */

        // verify email OTP (ACTIVE)
        boolean emailOtpOk = otpService.verifyOtp(req.getEmail(), req.getEmailOtp(), OtpType.EMAIL);
        if (!emailOtpOk) {
            model.addAttribute("error", "Invalid or expired email OTP");
            return "user/register";
        }

        // verify phone OTP (DISABLED)
        /*
        boolean phoneOtpOk = true;
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()) {
            phoneOtpOk = otpService.verifyOtp(req.getPhoneNumber(), req.getPhoneOtp(), OtpType.PHONE);
            if (!phoneOtpOk) {
                model.addAttribute("error", "Invalid or expired phone OTP");
                return "user/register";
            }
        }
        */

        // create user (phoneNumber is optional, not verified)
        UserRegistrationRequest userReq = new UserRegistrationRequest();
        userReq.setFullName(req.getFullName());
        userReq.setEmail(req.getEmail());
        userReq.setPhoneNumber(req.getPhoneNumber());
        userReq.setPassword(req.getPassword());
        userReq.setAddress(req.getAddress());

        userService.registerUser(userReq);

        model.addAttribute("message", "Registration successful! Please login.");
        return "redirect:/user/login";
    }

    // ---------- LOGIN PAGE ----------
    @GetMapping("/login")
    public String showLoginPage() {
        return "user/login";
    }

    // ---------- FORGOT PASSWORD ----------
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @ModelAttribute("forgotRequest") ForgotPasswordRequest req,
            Model model
    ) {
        Optional<User> userOpt = userService.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that email");
            return "user/forgot-password";
        }

        otpService.createAndSendOtp(req.getEmail(), OtpType.RESET_PASSWORD);
        model.addAttribute("email", req.getEmail());
        model.addAttribute("resetRequest", new ResetPasswordRequest());
        return "user/reset-password";
    }

    // ---------- RESET PASSWORD ----------
    @PostMapping("/reset-password")
    public String handleResetPassword(
            @ModelAttribute("resetRequest") ResetPasswordRequest req,
            Model model
    ) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("email", req.getEmail());
            return "user/reset-password";
        }

        Optional<User> userOpt = userService.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email");
            return "user/reset-password";
        }

        boolean otpOk = otpService.verifyOtp(req.getEmail(), req.getOtp(), OtpType.RESET_PASSWORD);
        if (!otpOk) {
            model.addAttribute("error", "Invalid or expired OTP");
            model.addAttribute("email", req.getEmail());
            return "user/reset-password";
        }

        userService.updatePassword(userOpt.get(), req.getNewPassword());
        model.addAttribute("message", "Password reset successful. Please login.");
        return "redirect:/user/login";
    }
}
