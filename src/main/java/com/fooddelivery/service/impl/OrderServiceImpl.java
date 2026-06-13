package com.fooddelivery.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.service.CartService;
import com.fooddelivery.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
    }

    @Override
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }

    @Override
    @Transactional
    public Order createOrderFromCart(User user, String deliveryAddress, String specialInstructions) {
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUser(user);
        order.setDeliveryAddress(deliveryAddress);
        order.setSpecialInstructions(specialInstructions);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFoodItem(cartItem.getFoodItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);

        order = orderRepository.save(order);

        cartService.clearCart(user);

        return order;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order setEstimatedTime(String orderId, Integer estimatedTimeMinutes) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setEstimatedTimeMinutes(estimatedTimeMinutes);
        return orderRepository.save(order);
    }

    private String generateOrderId() {
        return "ORD" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
