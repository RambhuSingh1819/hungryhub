package com.fooddelivery.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fooddelivery.dto.LoginRequest;
import com.fooddelivery.dto.OtpRequest;
import com.fooddelivery.dto.OtpVerifyRequest;
import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OtpVerification;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.CartService;
import com.fooddelivery.service.FoodItemService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.OtpService;
import com.fooddelivery.service.PaymentService;
import com.fooddelivery.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OtpService otpService;
    private final FoodItemService foodItemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    // ---------- AUTH PAGES ----------

    @GetMapping("/register")
    public String showRegisterPage() {
        return "user/register";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "user/login";
    }

    // ---------- DASHBOARD ----------
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);
        return "user/dashboard";
    }

    // ---------- MENU PAGE ----------
    @GetMapping("/menu")
    public String showMenu(@RequestParam(value = "search", required = false) String search,
                           Model model,
                           HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<FoodItem> items;
        if (search != null && !search.isBlank()) {
            items = foodItemService.searchItems(search);
        } else {
            items = foodItemService.getAllAvailableItems();
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search);

        return "user/menu";
    }

    // ---------- CART PAGE ----------
    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        Cart cart = cartService.getOrCreateCart(user);

        BigDecimal total = BigDecimal.ZERO;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                if (item.getTotalPrice() != null) {
                    total = total.add(item.getTotalPrice());
                }
            }
        }

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);

        return "user/cart";
    }

    // Add-to-cart endpoint used by menu.html (POST /user/cart/add)
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("foodId") Long foodId,
                            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        cartService.addItemToCart(user, foodId, 1);
        return "redirect:/user/cart";
    }

    // ---------- CART AJAX: UPDATE QUANTITY ----------
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {

        Map<String, Object> res = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.put("success", false);
            res.put("message", "Please login first");
            return ResponseEntity.status(401).body(res);
        }

        try {
            if (quantity != null && quantity > 0) {
                cartService.updateCartItemQuantity(user, cartItemId, quantity);
            } else {
                cartService.removeItemFromCart(user, cartItemId);
            }
            res.put("success", true);
            res.put("message", "Cart updated");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ---------- CART AJAX: REMOVE ITEM ----------
    @PostMapping("/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            HttpSession session) {

        Map<String, Object> res = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.put("success", false);
            res.put("message", "Please login first");
            return ResponseEntity.status(401).body(res);
        }

        try {
            cartService.removeItemFromCart(user, cartItemId);
            res.put("success", true);
            res.put("message", "Item removed");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    // ---------- ORDERS PAGE ----------
    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("orders", orderService.getOrdersForUser(user.getId()));
        return "user/orders";
    }

    // ---------- LOGOUT ----------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }

    // ---------- SEND OTP (for registration) ----------
    @PostMapping("/send-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody OtpRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                if (userService.existsByEmail(request.getEmail())) {
                    response.put("success", false);
                    response.put("message", "Email already registered");
                    return ResponseEntity.badRequest().body(response);
                }
                otpService.createAndSendOtp(request.getEmail(), OtpVerification.OtpType.EMAIL);
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

    // ---------- VERIFY OTP (for registration) ----------
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
                        OtpVerification.OtpType.EMAIL
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

    // ---------- REGISTER ----------
    @PostMapping("/register")
    @ResponseBody
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

    // ---------- LOGIN ----------
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = null;

            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                user = userService.findByEmail(request.getEmail()).orElse(null);
            }

            if (user == null || !userService.validatePassword(request.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                return ResponseEntity.badRequest().body(response);
            }

            session.setAttribute("user", user);
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("redirect", "/user/dashboard");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

 // ---------- CREATE ORDER FROM CART (for checkout) ----------
    @PostMapping("/orders/create-from-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @RequestBody(required = false) Map<String, String> request,
            @RequestParam(value = "deliveryAddress", required = false) String deliveryAddressParam,
            @RequestParam(value = "specialInstructions", required = false) String specialInstructionsParam,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // --- User check ---
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login first");
            return ResponseEntity.status(401).body(response);
        }

        // --- Input extraction (JSON body ya form params se) ---
        String deliveryAddress = null;
        String specialInstructions = "";

        if (request != null) {
            // JSON body se
            deliveryAddress = request.get("deliveryAddress");
            specialInstructions = request.getOrDefault("specialInstructions", "");
        } else {
            // Agar kisi request me body nahi aayi (form submit / kuch aur)
            deliveryAddress = deliveryAddressParam;
            specialInstructions = (specialInstructionsParam != null) ? specialInstructionsParam : "";
        }

        // --- Validation ---
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            response.put("success", false);
            response.put("message", "Delivery address is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Order order = orderService.createOrderFromCart(user, deliveryAddress, specialInstructions);

            response.put("success", true);
            response.put("message", "Order created");
            response.put("orderId", order.getOrderId());
            response.put("amount", order.getTotalAmount());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        return "user/checkout";
    }


    // Payment / checkout endpoints can go here using paymentService
}
