package com.fooddelivery.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.UserService;
import com.fooddelivery.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    private User getUserFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return null;
        }
        String email = jwtTokenProvider.getEmailFromToken(token);
        return userService.findByEmail(email).orElse(null);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        List<Order> orders = orderService.getOrdersForUser(user.getId());
        res.put("success", true);
        res.put("orders", orders);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/create-from-cart")
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        String deliveryAddress = body != null ? body.get("deliveryAddress") : null;
        String specialInstructions = body != null ? body.getOrDefault("specialInstructions", "") : "";

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            res.put("success", false);
            res.put("message", "Delivery address is required");
            return ResponseEntity.badRequest().body(res);
        }

        try {
            Order order = orderService.createOrderFromCart(user, deliveryAddress, specialInstructions);
            res.put("success", true);
            res.put("message", "Order placed successfully");
            res.put("orderId", order.getOrderId());
            res.put("totalAmount", order.getTotalAmount());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Failed to place order: " + e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
