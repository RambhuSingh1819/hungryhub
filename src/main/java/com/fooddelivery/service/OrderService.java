package com.fooddelivery.service;

import java.util.List;
import java.util.Optional;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;

public interface OrderService {
    Optional<Order> findByOrderId(String orderId);
    Optional<Order> findById(Long id);
    List<Order> getOrdersForUser(Long userId);
    List<Order> getAllOrders();
    List<Order> getOrdersByStatus(Order.OrderStatus status);
    Order createOrderFromCart(User user, String deliveryAddress, String specialInstructions);
    Order updateOrderStatus(String orderId, Order.OrderStatus status);
    Order setEstimatedTime(String orderId, Integer estimatedTimeMinutes);
}
