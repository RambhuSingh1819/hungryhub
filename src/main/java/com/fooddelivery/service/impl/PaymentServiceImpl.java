package com.fooddelivery.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.service.PaymentService;
import com.fooddelivery.service.payment.PaymentGateway;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    @Override
    public CreateOrderResponse createRazorpayOrder(CreateOrderRequest req) throws Exception {
        return paymentGateway.createOrder(req);
    }

    @Override
    public boolean verifyPaymentSignature(VerifyPaymentRequest req) {
        return paymentGateway.verifySignature(req);
    }

    @Override
    @Transactional
    public void finalizePayment(VerifyPaymentRequest req, boolean isValid) {
        if (req.getAppOrderId() == null) {
            return;
        }

        Order order = orderRepository.findByOrderId(req.getAppOrderId()).orElse(null);
        if (order == null) {
            return;
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElse(new Payment());

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setTransactionId(req.getRazorpayPaymentId());
        payment.setPaymentMethod(Payment.PaymentMethod.RAZORPAY);
        payment.setPaymentDate(LocalDateTime.now());

        if (isValid) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setRazorpayPaymentId(req.getRazorpayPaymentId());
            order.setRazorpaySignature(req.getRazorpaySignature());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}
