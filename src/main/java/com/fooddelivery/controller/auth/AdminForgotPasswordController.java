//package com.fooddelivery.controller.auth;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.fooddelivery.dto.ForgotPasswordRequest;
//import com.fooddelivery.dto.ResetPasswordRequest;
//import com.fooddelivery.entity.OtpVerification;
//import com.fooddelivery.service.AdminService;
//import com.fooddelivery.service.OtpService;
//
//import lombok.RequiredArgsConstructor;
//
//@Controller
//@RequestMapping("/admin")
//@RequiredArgsConstructor
//public class AdminForgotPasswordController {
//
//    private final AdminService adminService;
//    private final OtpService otpService;
//
//    // ---------- SEND OTP (form submit) ----------
//    @PostMapping("/forgot-password")
//    public String sendOtp(
//            @ModelAttribute("forgotRequest") ForgotPasswordRequest req,
//            Model model
//    ) {
//
//        if (!adminService.existsByEmail(req.getEmail())) {
//            model.addAttribute("error", "Admin not found");
//            return "admin/forgot-password";
//        }
//
//        otpService.createAndSendOtp(
//                req.getEmail(),
//                OtpVerification.OtpType.ADMIN_RESET_PASSWORD
//        );
//
//        model.addAttribute("email", req.getEmail());
//        model.addAttribute("resetRequest", new ResetPasswordRequest());
//        return "admin/reset-password";
//    }
//
//    // ---------- VERIFY OTP + RESET PASSWORD ----------
//    @PostMapping("/reset-password")
//    public String verifyOtp(
//            @ModelAttribute("resetRequest") ResetPasswordRequest req,
//            Model model
//    ) {
//
//        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
//            model.addAttribute("error", "Passwords do not match");
//            model.addAttribute("email", req.getEmail());
//            return "admin/reset-password";
//        }
//
//        boolean valid = otpService.verifyOtp(
//                req.getEmail(),
//                req.getOtp(),
//                OtpVerification.OtpType.ADMIN_RESET_PASSWORD
//        );
//
//        if (!valid) {
//            model.addAttribute("error", "Invalid or expired OTP");
//            model.addAttribute("email", req.getEmail());
//            return "admin/reset-password";
//        }
//
//        adminService.resetPassword(req.getEmail(), req.getNewPassword());
//
//        return "redirect:/admin/login";
//    }
//}
