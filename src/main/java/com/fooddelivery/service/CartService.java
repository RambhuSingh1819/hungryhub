package com.fooddelivery.service;

import java.math.BigDecimal;
import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.User;

public interface CartService {
    Cart getOrCreateCart(User user);
    Cart addItemToCart(User user, Long foodItemId, Integer quantity);
    Cart updateCartItemQuantity(User user, Long cartItemId, Integer quantity);
    Cart removeItemFromCart(User user, Long cartItemId);
    void clearCart(User user);
    BigDecimal calculateCartTotal(Cart cart);
}
