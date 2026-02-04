package com.fooddelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fooddelivery.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders for a specific user
    List<Order> findByUserId(Long userId);

    // Get orders by status (for admin)
    List<Order> findAllByStatus(Order.OrderStatus status);

    // Get order using internal order-id (ex: ORD78A1D3F4C22)
    Optional<Order> findByOrderId(String orderId);
}
