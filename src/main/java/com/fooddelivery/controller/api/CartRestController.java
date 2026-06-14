package com.fooddelivery.controller.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.User;
import com.fooddelivery.service.CartService;
import com.fooddelivery.service.UserService;
import com.fooddelivery.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartRestController {

    private final CartService cartService;
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
    public ResponseEntity<Map<String, Object>> getCart(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized. Please provide a valid Bearer token.");
            return ResponseEntity.status(401).body(res);
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

        res.put("success", true);
        res.put("cart", cart);
        res.put("total", total);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("foodId") Long foodId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        try {
            cartService.addItemToCart(user, foodId, quantity);
            res.put("success", true);
            res.put("message", "Item added to cart successfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            @RequestParam("quantity") Integer quantity,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        try {
            if (quantity != null && quantity > 0) {
                cartService.updateCartItemQuantity(user, cartItemId, quantity);
            } else {
                cartService.removeItemFromCart(user, cartItemId);
            }
            res.put("success", true);
            res.put("message", "Cart item updated successfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @RequestParam("cartItemId") Long cartItemId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> res = new HashMap<>();
        User user = getUserFromAuthHeader(authHeader);
        if (user == null) {
            res.put("success", false);
            res.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(res);
        }

        try {
            cartService.removeItemFromCart(user, cartItemId);
            res.put("success", true);
            res.put("message", "Item removed from cart");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
    }
}
