package com.fooddelivery.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.dto.ResetPasswordRequest;
import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.service.OtpService;
import com.fooddelivery.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/password")
@RequiredArgsConstructor
public class UserForgotPasswordController {

    private final UserService userService;
    private final OtpService otpService;

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @ModelAttribute("resetRequest") ResetPasswordRequest req,Model model) {

        boolean valid = otpService.verifyOtp(
                req.getEmail(),
                req.getOtp(),
                OtpVerification.OtpType.RESET_PASSWORD
        );

        if (!valid) {
            model.addAttribute("error", "Invalid or expired OTP");
            model.addAttribute("resetRequest", req);
            return "user/reset-password";
        }

        userService.resetPassword(req.getEmail(), req.getNewPassword());

        return "redirect:/user/login";
    }
}
