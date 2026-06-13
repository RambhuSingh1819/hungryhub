package com.fooddelivery.service.impl;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.CartItemRepository;
import com.fooddelivery.repository.CartRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;

    @Override
    public Cart getOrCreateCart(User user) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart addItemToCart(User user, Long foodItemId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        if (!foodItem.isAvailable()) {
            throw new RuntimeException("Food item is not available");
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setFoodItem(foodItem);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(foodItem.getPrice());
            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateCartItemQuantity(User user, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user's cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeItemFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user's cart");
        }

        cartItemRepository.delete(cartItem);
        cart.getItems().remove(cartItem);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
