package com.fooddelivery.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.entity.Admin;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.PaymentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final AdminService adminService;

    // ================= USER ORDER PAYMENT (already used by cart.js) =================
    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            CreateOrderResponse response = paymentService.createRazorpayOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error creating Razorpay order: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ================= ADMIN SUBSCRIPTION PAYMENT =================

    @PostMapping("/admin/create-subscription-order")
    @ResponseBody
    public ResponseEntity<?> createAdminSubscriptionOrder(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Please login as admin"));
        }

        try {
            BigDecimal amount = new BigDecimal("499.00"); // 💰 subscription fee

            CreateOrderRequest req = new CreateOrderRequest();
            req.setAmount(amount);

            // ADMIN_SUB_{adminId}_{timestamp}
            String appOrderId = "ADMIN_SUB_" + admin.getId() + "_" + System.currentTimeMillis();
            req.setAppOrderId(appOrderId);

            CreateOrderResponse resp = paymentService.createRazorpayOrder(req);
            // make sure response also carries appOrderId back to JS
            resp.setAppOrderId(appOrderId);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("razorpayKeyId", resp.getRazorpayKeyId());
            body.put("razorpayOrderId", resp.getRazorpayOrderId());
            body.put("amountInPaise", resp.getAmountInPaise());
            body.put("currency", resp.getCurrency());
            body.put("appOrderId", resp.getAppOrderId());

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error creating admin subscription order: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ================= VERIFY PAYMENT (common for user + admin) =================

    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyPaymentRequest request) {

        boolean valid = paymentService.verifyPaymentSignature(request);
        paymentService.finalizePayment(request, valid); // tumhara existing logic

        // 👉 Extra logic: if this is admin subscription, mark as paid
        if (valid && request.getAppOrderId() != null
                && request.getAppOrderId().startsWith("ADMIN_SUB_")) {

            Long adminId = extractAdminIdFromAppOrderId(request.getAppOrderId());
            // 1 month subscription example
            adminService.markAdminSubscriptionPaid(
                    adminId,
                    "MONTHLY",
                    LocalDate.now().plusMonths(1)
            );
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", valid);

        if (valid) {
            res.put("message", "Payment Success");
            return ResponseEntity.ok(res);
        } else {
            res.put("message", "Invalid signature");
            return ResponseEntity.badRequest().body(res);
        }
    }

    private Long extractAdminIdFromAppOrderId(String appOrderId) {
        // Format: ADMIN_SUB_{adminId}_{timestamp}
        try {
            String[] parts = appOrderId.split("_");
            return Long.parseLong(parts[2]); // index 0=ADMIN,1=SUB,2=adminId
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid appOrderId format: " + appOrderId);
        }
    }

    @GetMapping("/success")
    public String success() {
        return "payment/success";
    }

    @GetMapping("/failed")
    public String failed() {
        return "payment/failed";
    }
}
