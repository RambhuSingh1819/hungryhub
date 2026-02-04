package com.fooddelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.CreateOrderResponse;
import com.fooddelivery.dto.VerifyPaymentRequest;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    // =========================
    // 1) Create Razorpay order
    // =========================
    public CreateOrderResponse createRazorpayOrder(CreateOrderRequest req) throws RazorpayException {

        BigDecimal rupees = req.getAmount();
        if (rupees == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        int amountInPaise = rupees.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", currency);
        options.put("payment_capture", 1);

        // ✅ use Razorpay SDK's Order type with fully-qualified name
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

        // link Razorpay order id to our Order (if provided)
        if (req.getAppOrderId() != null) {
            // ✅ use internal orderId (String) instead of DB id
            orderRepository.findByOrderId(req.getAppOrderId()).ifPresent(order -> {
                order.setRazorpayOrderId(razorpayOrder.get("id"));
                order.setPaymentStatus(Order.PaymentStatus.PROCESSING);
                orderRepository.save(order);
            });
        }

        return null ;
    }

    // =========================
    // 2) Verify signature
    // =========================
    public boolean verifyPaymentSignature(VerifyPaymentRequest req) {
        try {
            String data = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
            String generated = hmacSha256(data, keySecret);
            return generated.equals(req.getRazorpaySignature());
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // 3) Finalize payment
    // =========================
    @Transactional
    public void finalizePayment(VerifyPaymentRequest req, boolean isValid) {

        if (req.getAppOrderId() == null) {
            return;
        }

        // ✅ again, use internal orderId (String)
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

    // =========================
    // Utility: HMAC-SHA256
    // =========================
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) {
                hex.append('0');
            }
            hex.append(h);
        }
        return hex.toString();
    }
}
