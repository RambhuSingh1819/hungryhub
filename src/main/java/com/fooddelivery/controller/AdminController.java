package com.fooddelivery.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fooddelivery.dto.AdminRegistrationRequest;
import com.fooddelivery.dto.AiFoodSuggestionRequest;
import com.fooddelivery.dto.AiFoodSuggestionResponse;
import com.fooddelivery.dto.ForgotPasswordRequest;
import com.fooddelivery.dto.LoginRequest;
import com.fooddelivery.dto.OtpRequest;
import com.fooddelivery.dto.OtpVerifyRequest;
import com.fooddelivery.dto.ResetPasswordRequest;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.service.AiService;
import com.fooddelivery.service.FoodItemService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.OtpService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OtpService otpService;
    private final FoodItemService foodItemService;
    private final OrderService orderService;
    private final AiService aiService;

    // ================== AUTH PAGES ==================

    @GetMapping("/register")
    public String showRegisterPage() {
        return "admin/register";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    // ================== ADMIN FORGOT PASSWORD ==================

    @GetMapping("/forgot-password")
    public String showAdminForgotPassword(Model model) {
        model.addAttribute("forgotRequest", new ForgotPasswordRequest());
        return "admin/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleAdminForgotPassword(
            @ModelAttribute("forgotRequest") ForgotPasswordRequest req,
            Model model
    ) {
        Optional<Admin> adminOpt = adminService.findByEmail(req.getEmail());
        if (adminOpt.isEmpty()) {
            model.addAttribute("error", "No admin found with this email");
            return "admin/forgot-password";
        }

        otpService.createAndSendOtp(req.getEmail(), OtpVerification.OtpType.ADMIN_RESET_PASSWORD);

        model.addAttribute("email", req.getEmail());
        model.addAttribute("resetRequest", new ResetPasswordRequest());

        return "admin/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleAdminResetPassword(
            @ModelAttribute("resetRequest") ResetPasswordRequest req,
            Model model
    ) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("email", req.getEmail());
            return "admin/reset-password";
        }

        Optional<Admin> adminOpt = adminService.findByEmail(req.getEmail());
        if (adminOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email");
            return "admin/reset-password";
        }

        boolean otpOk = otpService.verifyOtp(req.getEmail(), req.getOtp(), OtpVerification.OtpType.ADMIN_RESET_PASSWORD);
        if (!otpOk) {
            model.addAttribute("error", "Invalid or expired OTP");
            model.addAttribute("email", req.getEmail());
            return "admin/reset-password";
        }

        // ✅ Reset password properly
        adminService.updatePassword(adminOpt.get(), req.getNewPassword());

        model.addAttribute("message", "Password reset successfully! Please login.");
        return "redirect:/admin/login";
    }

    // ================== OTP HANDLING ==================

    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody OtpRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                if (adminService.existsByEmail(request.getEmail())) {
                    response.put("success", false);
                    response.put("message", "Email already registered");
                    return ResponseEntity.badRequest().body(response);
                }
                otpService.createAndSendOtp(request.getEmail(), OtpVerification.OtpType.ADMIN_EMAIL);
            }

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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean emailVerified = true;

            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                emailVerified = otpService.verifyOtp(
                        request.getEmail(),
                        request.getOtp(),
                        OtpVerification.OtpType.ADMIN_EMAIL
                );
            }

            if (emailVerified) {
                response.put("success", true);
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid OTP");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error verifying OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================== REGISTER & LOGIN ==================

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(@RequestBody AdminRegistrationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = adminService.registerAdmin(request);
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("adminId", admin.getAdminId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = null;

            if (request.getAdminId() != null && !request.getAdminId().isEmpty()) {
                admin = adminService.findByAdminId(request.getAdminId()).orElse(null);
            } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                admin = adminService.findByEmail(request.getEmail()).orElse(null);
            }

            if (admin == null || !adminService.validatePassword(request.getPassword(), admin.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                return ResponseEntity.badRequest().body(response);
            }

            session.setAttribute("admin", admin);
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("redirect", "/admin/dashboard");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================== DASHBOARD ==================

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // 👉 SPECIAL CASE: is email ke liye subscription check mat karo
        if (!"rambhusingh1819@gmail.com".equalsIgnoreCase(admin.getEmail())) {
            // 🔐 baaki sab admins ke liye subscription check
            if (!adminService.isSubscriptionActive(admin)) {
                return "redirect:/admin/pay";
            }
        }

        model.addAttribute("admin", admin);
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/dashboard";
    }

 // ================== ADMIN PAY PAGE (NEW) ==================

    @GetMapping("/pay")
    public String showAdminPayPage(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // 👉 SPECIAL CASE: is email ko pay page pe mat aane do
        if ("rambhusingh1819@gmail.com".equalsIgnoreCase(admin.getEmail())) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("admin", admin);
        model.addAttribute("amount", new BigDecimal("499.00")); // subscription amount
        return "admin/pay";
    }


    // ================== FOOD ITEMS ==================

    @GetMapping("/food-items")
    public String foodItems(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("items", foodItemService.getAllAvailableItems());
        return "admin/food-items";
    }

    // ---------- AI ENDPOINT ----------
    @PostMapping("/food-items/ai-suggest")
    @ResponseBody
    public ResponseEntity<AiFoodSuggestionResponse> suggestFoodItemWithAi(
            @RequestBody AiFoodSuggestionRequest request,
            HttpSession session
    ) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return ResponseEntity.status(401).body(
                    AiFoodSuggestionResponse.builder()
                            .description("Please login first.")
                            .imageUrl("")
                            .suggestions(List.of())
                            .build()
            );
        }

        try {
            return ResponseEntity.ok(
                    aiService.generateFoodSuggestion(request.getName(), request.getCategory())
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    AiFoodSuggestionResponse.builder()
                            .description("Tasty " + request.getName())
                            .imageUrl("")
                            .suggestions(List.of("Cheese " + request.getName()))
                            .build()
            );
        }
    }

    // ================== ADD / UPDATE / DELETE FOOD ITEMS ==================

    @PostMapping("/food-items/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFoodItem(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String imageUrl,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.badRequest().body(response);
            }

            FoodItem item = new FoodItem();
            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);
            item.setCategory(category);
            item.setImageUrl(imageUrl);
            item.setAvailable(true);

            foodItemService.saveItem(item);

            response.put("success", true);
            response.put("message", "Food item added successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding food item: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/food-items/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateFoodItem(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) Boolean available,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.badRequest().body(response);
            }

            FoodItem item = foodItemService.getItemById(id)
                    .orElseThrow(() -> new RuntimeException("Food item not found"));

            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);

            if (category != null) {
                item.setCategory(category);
            }
            if (imageUrl != null) {
                item.setImageUrl(imageUrl);
            }
            if (available != null) {
                item.setAvailable(available);
            }

            foodItemService.saveItem(item);

            response.put("success", true);
            response.put("message", "Food item updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating food item: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/food-items/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFoodItem(
            @RequestParam Long id,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.badRequest().body(response);
            }

            foodItemService.deleteItem(id);

            response.put("success", true);
            response.put("message", "Food item deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting food item: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================== ORDERS ==================

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<Order> orders = orderService.getAllOrders();

        long activeOrdersCount = orders.stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.DELIVERED)
                .count();

        long pendingOrdersCount = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .count();

        model.addAttribute("orders", orders);
        model.addAttribute("activeOrdersCount", activeOrdersCount);
        model.addAttribute("pendingOrdersCount", pendingOrdersCount);

        return "admin/orders";
    }


    @PostMapping("/orders/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestParam String orderId,
            @RequestParam String status,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.badRequest().body(response);
            }

            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
            orderService.updateOrderStatus(orderId, orderStatus);

            response.put("success", true);
            response.put("message", "Order status updated");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating order status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/orders/set-time")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setEstimatedTime(
            @RequestParam String orderId,
            @RequestParam Integer estimatedTimeMinutes,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.badRequest().body(response);
            }

            orderService.setEstimatedTime(orderId, estimatedTimeMinutes);

            response.put("success", true);
            response.put("message", "Estimated time set");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error setting estimated time: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ================== LOGOUT ==================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
